//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.pwa;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class GeneratePwaTask extends DefaultTask {

    @TaskAction
    public void run() {
        PwaExt config = getProject().getExtensions().getByType(PwaExt.class);
        run(config);
    }

    protected void run(PwaExt config) {
        config.validate();

        getProject().copy(copy -> {
            copy.from(config.getWebAppDir());
            copy.into(config.getOutputDir(getProject()));
            copy.exclude("build/**");
        });

        try {
            File indexFile = new File(config.getOutputDir(getProject()), "index.html");
            rewriteHTML(indexFile);
            writeManifest(config);
            writeServiceWorker(config);
        } catch (IOException e) {
            throw new RuntimeException("Error while generating PWA", e);
        }
    }

    private String loadResourceFile(String path) {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
            byte[] contents = stream.readAllBytes();
            return new String(contents, UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Resource file not found: " + path);
        }
    }

    private void rewriteHTML(File indexFile) throws IOException {
        String manifestSnippet = "<link rel=\"manifest\" href=\"manifest.json\" />\n";
        String serviceWorkerSnippet = loadResourceFile("service-worker.html");

        String html = Files.readString(indexFile.toPath(), UTF_8);
        html = html.replace("</head>", manifestSnippet + "</head>");
        html = html.replace("</body>", serviceWorkerSnippet + "</body>");

        Files.writeString(indexFile.toPath(), html, UTF_8);
    }

    private void writeManifest(PwaExt config) throws IOException {
        String manifest = Files.readString(new File(config.getManifest()).toPath(), UTF_8);
        File outputFile = new File(config.getOutputDir(getProject()), "manifest.json");
        Files.writeString(outputFile.toPath(), manifest, UTF_8);
    }

    private void writeServiceWorker(PwaExt config) throws IOException {
        String serviceWorker = prepareServiceWorker(config);
        File outputFile = new File(config.getOutputDir(getProject()), "service-worker.js");
        Files.writeString(outputFile.toPath(), serviceWorker,  UTF_8);
    }

    private String prepareServiceWorker(PwaExt config) throws IOException {
        if (config.getServiceWorker() != null) {
            return Files.readString(new File(config.getServiceWorker()).toPath(), UTF_8);
        }

        Path base = config.getOutputDir(getProject()).toPath();

        String resourceFileList = Files.walk(base)
            .filter(file -> !Files.isDirectory(file))
            .map(file -> base.relativize(file).toString())
            .filter(file -> !file.startsWith("userHome"))
            .sorted()
            .map(file -> "\"" + file + "\",\n")
            .collect(Collectors.joining(""));

        String js = loadResourceFile("service-worker.js");
        js = js.replace("{{cacheName}}", config.getCacheName());
        js = js.replace("{{resourceFiles}}", resourceFileList);
        return js;
    }
}
