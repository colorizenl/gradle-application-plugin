//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.pwa;

import nl.colorize.gradle.application.ApplicationPlugin;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratePwaTaskTest {

    @Test
    void rewriteHTML(@TempDir File tempDir) throws IOException {
        String html = """
            <html>
                <head>
                    <title>Test</title>
                </head>
                <body>
                    Hello world!
                </body>
            </html>
            """;

        Files.writeString(new File(tempDir, "manifest.json").toPath(), "{}", UTF_8);
        Files.writeString(new File(tempDir, "index.html").toPath(), html, UTF_8);

        PwaExt config = new PwaExt();
        config.setWebAppDir(tempDir.getAbsolutePath());
        config.setManifest(new File(tempDir, "manifest.json").getAbsolutePath());
        config.setCacheName("test");

        Project project = initProject(tempDir);
        GeneratePwaTask task = (GeneratePwaTask) project.getTasks().getByName("generatePWA");
        task.run(config);

        String expected = """
            <html>
                <head>
                    <title>Test</title>
                <link rel="manifest" href="manifest.json" />
            </head>
                <body>
                    Hello world!
                <script>
                const serviceWorkerSupported = typeof navigator.serviceWorker != "undefined";
                const localFile = window.location.protocol.indexOf("file") != -1;
                        
                if (serviceWorkerSupported && !localFile) {
                    navigator.serviceWorker.register("service-worker.js");
                }
            </script>
            </body>
            </html>
            """;

        assertEquals(expected, readOutput(tempDir, "build/pwa/index.html"));
        assertTrue(new File(tempDir, "build/pwa/manifest.json").exists());
        assertTrue(new File(tempDir, "build/pwa/service-worker.js").exists());
    }

    @Test
    void generateServiceWorker(@TempDir File tempDir) throws IOException {
        Files.writeString(new File(tempDir, "manifest.json").toPath(), "{}", UTF_8);
        Files.writeString(new File(tempDir, "index.html").toPath(), "<head></head><body></body>", UTF_8);

        PwaExt config = new PwaExt();
        config.setWebAppDir(tempDir.getAbsolutePath());
        config.setManifest(new File(tempDir, "manifest.json").getAbsolutePath());
        config.setCacheName("test");

        Project project = initProject(tempDir);
        GeneratePwaTask task = (GeneratePwaTask) project.getTasks().getByName("generatePWA");
        task.run(config);

        String expected = """
            //-----------------------------------------------------------------------------
            // File generated by Colorize Gradle application plugin
            //-----------------------------------------------------------------------------
            
            const CACHE_NAME = "test";
                        
            const RESOURCE_FILES = [
                "/",
                "/index.html",
            "/manifest.json",
                        
            ];
                        
            self.addEventListener("install", event => {
                event.waitUntil(
                    caches.open(CACHE_NAME).then(cache => {
                        return cache.addAll(RESOURCE_FILES);
                    })
                );
            });
                        
            self.addEventListener("fetch", event => {
                event.respondWith(
                    fetch(event.request).catch(() => {
                        return caches.match(event.request);
                    })
                );
            });
            """;

        assertEquals(expected, readOutput(tempDir, "build/pwa/service-worker.js"));
    }

    @Test
    void clearOutputDirectory(@TempDir File tempDir) throws IOException {
        Files.writeString(new File(tempDir, "manifest.json").toPath(), "{}", UTF_8);
        Files.writeString(new File(tempDir, "index.html").toPath(), "<html />", UTF_8);
        Files.writeString(new File(tempDir, "new.txt").toPath(), "1234", UTF_8);

        File buildDir = new File(tempDir, "build/pwa");
        buildDir.mkdirs();
        Files.writeString(new File(buildDir, "old.txt").toPath(), "1234", UTF_8);

        PwaExt config = new PwaExt();
        config.setWebAppDir(tempDir.getAbsolutePath());
        config.setManifest(new File(tempDir, "manifest.json").getAbsolutePath());
        config.setCacheName("test");

        Project project = initProject(tempDir);
        GeneratePwaTask task = (GeneratePwaTask) project.getTasks().getByName("generatePWA");
        task.run(config);

        assertTrue(new File(buildDir, "new.txt").exists());
        assertFalse(new File(buildDir, "old.txt").exists());
    }

    private Project initProject(File inputDir) {
        Project project = ProjectBuilder.builder()
            .withProjectDir(inputDir)
            .build();

        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);
        return project;
    }

    private String readOutput(File tempDir, String name) throws IOException {
        return Files.readString(new File(tempDir, name).toPath(), UTF_8);
    }
}
