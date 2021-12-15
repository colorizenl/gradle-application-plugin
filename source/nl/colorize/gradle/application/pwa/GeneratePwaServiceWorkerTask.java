//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.pwa;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class GeneratePwaServiceWorkerTask extends DefaultTask {

    @TaskAction
    public void run() {
        PwaExt config = getProject().getExtensions().getByType(PwaExt.class);
        run(config);
    }

    protected void run(PwaExt config) {
        try (PrintWriter writer = new PrintWriter(config.getServiceWorkerFile(), UTF_8)) {
            writer.println("const CACHE_KEY = \"" + config.getPwaName() + "-" + config.getPwaVersion() + "\";");
            writer.println();
            writer.println("const RESOURCE_FILES = [");
            for (String resourceFilePath : locateResourceFilePaths(config)) {
                writer.println("    \"" + resourceFilePath + "\",");
            }
            writer.println("];");
            writer.println();
            writer.println("self.addEventListener(\"install\", e => {");
            writer.println("    e.waitUntil((async () => {");
            writer.println("        const cache = await caches.open(CACHE_KEY);");
            writer.println("        await cache.addAll(RESOURCE_FILES);");
            writer.println("    })());");
            writer.println("});");
        } catch (IOException e) {
            throw new RuntimeException("Cannot write to file: " + config.getServiceWorkerFile(), e);
        }
    }

    private List<String> locateResourceFilePaths(PwaExt config) throws IOException {
        return Files.walk(config.getWebAppDir().toPath())
            .map(Path::toFile)
            .filter(file -> !file.isDirectory())
            .map(file -> getResourceFilePath(file, config.getWebAppDir()))
            .sorted()
            .collect(Collectors.toList());
    }

    private String getResourceFilePath(File file, File base) {
        return "/" + base.toPath().relativize(file.toPath()).toString();
    }
}
