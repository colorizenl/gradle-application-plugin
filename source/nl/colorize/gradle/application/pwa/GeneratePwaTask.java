//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.pwa;

import nl.colorize.gradle.application.AppHelper;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class GeneratePwaTask extends DefaultTask {

    @Nested
    public abstract Property<PwaExt> getExt();

    @Inject
    public abstract FileSystemOperations getFileSystemOperations();

    @TaskAction
    public void run() {
        run(getExt().get());
    }

    protected void run(PwaExt config) {
        File inputDir = config.toProjectFile(config.getWebAppDir().get());
        File outputDir = config.toOutputDir(config.getOutputDir().get());

        AppHelper.cleanDirectory(outputDir);

        getFileSystemOperations().copy(copy -> {
            copy.from(inputDir);
            copy.into(outputDir);
            copy.exclude("build/**");
        });

        try {
            File indexFile = new File(outputDir, "index.html");
            rewriteHTML(indexFile);
            writeManifest(config);
            writeServiceWorker(config);
        } catch (IOException e) {
            throw new RuntimeException("Error while generating PWA", e);
        }
    }

    private void rewriteHTML(File indexFile) throws IOException {
        String manifestSnippet = "<link rel=\"manifest\" href=\"manifest.json\" />\n";
        String serviceWorkerSnippet = AppHelper.loadResourceFile("service-worker.html");

        String html = Files.readString(indexFile.toPath(), UTF_8);
        html = html.replace("</head>", manifestSnippet + "</head>");
        html = html.replace("</body>", serviceWorkerSnippet + "</body>");

        Files.writeString(indexFile.toPath(), html, UTF_8);
    }

    private void writeManifest(PwaExt config) throws IOException {
        String manifest = Files.readString(new File(config.getManifest().get()).toPath(), UTF_8);
        File outputDir = config.toOutputDir(config.getOutputDir().get());
        File outputFile = new File(outputDir, "manifest.json");
        Files.writeString(outputFile.toPath(), manifest, UTF_8);
    }

    private void writeServiceWorker(PwaExt config) throws IOException {
        String serviceWorker = prepareServiceWorker(config);
        File outputDir = config.toOutputDir(config.getOutputDir().get());
        File outputFile = new File(outputDir, "service-worker.js");
        Files.writeString(outputFile.toPath(), serviceWorker,  UTF_8);
    }

    private String prepareServiceWorker(PwaExt config) throws IOException {
        if (!config.getServiceWorker().get().isEmpty()) {
            File customServiceWorkerFile = config.toProjectFile(config.getServiceWorker().get());
            return Files.readString(customServiceWorkerFile.toPath(), UTF_8);
        }

        File outputDir = config.toOutputDir(config.getOutputDir().get());
        List<String> resourceFiles = getResourceFileList(outputDir.toPath());

        return AppHelper.rewriteTemplate("service-worker.js", Map.of(
            "{{cacheName}}", config.getCacheName().get(),
            "{{resourceFiles}}", String.join("", resourceFiles)
        ));
    }

    private List<String> getResourceFileList(Path baseDir) throws IOException {
        try (Stream<Path> stream = Files.walk(baseDir)) {
            return stream.filter(file -> !Files.isDirectory(file))
                .map(file -> baseDir.relativize(file).toString())
                .filter(file -> !file.startsWith("userHome"))
                .sorted()
                .map(file -> "\"/" + file + "\",\n")
                .toList();
        }
    }
}
