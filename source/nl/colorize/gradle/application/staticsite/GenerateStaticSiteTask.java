//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.staticsite;

import nl.colorize.gradle.application.AppHelper;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static nl.colorize.gradle.application.staticsite.StaticSiteExt.TEMPLATE_TAGS;

public class GenerateStaticSiteTask extends DefaultTask {

    private Map<File, Document> templateCache;

    public GenerateStaticSiteTask() {
        this.templateCache = new HashMap<>();
    }

    @TaskAction
    public void run() {
        StaticSiteExt config = getProject().getExtensions().getByType(StaticSiteExt.class);
        run(config);
    }

    protected void run(StaticSiteExt config) {
        File contentDir = new File(getProject().getProjectDir(), config.getContentDir());
        File outputDir = config.getOutputDir(getProject());

        reset(outputDir);

        try {
            for (File file : traverse(contentDir, file -> isTemplateFile(file, config))) {
                Document template = Jsoup.parse(file);
                templateCache.put(file, template);
            }

            for (File file : traverse(contentDir, file -> !isTemplateFile(file, config))) {
                processFile(file, contentDir, outputDir, config);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while generating static site", e);
        }
    }

    private void reset(File outputDir) {
        if (!getProject().getBuildDir().exists()) {
            getProject().getBuildDir().mkdir();
        }

        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        AppHelper.cleanDirectory(outputDir);

        templateCache.clear();
    }

    private List<File> traverse(File contentDir, Predicate<File> filter) throws IOException {
        return Files.walk(contentDir.toPath())
            .map(Path::toFile)
            .distinct()
            .filter(file -> !isIgnored(file))
            .filter(file -> !file.equals(contentDir))
            .filter(filter)
            .toList();
    }

    private void processFile(File file, File contentDir, File outputDir, StaticSiteExt config)
            throws IOException {
        Path relativePath = contentDir.toPath().relativize(file.toPath());
        File outputFile = outputDir.toPath().resolve(relativePath).toFile();

        if (file.isDirectory()) {
            outputFile.mkdir();
        } else if (file.getName().endsWith(".html")) {
            String content = Files.readString(file.toPath(), UTF_8);
            processContentFile(content, file, outputFile, config);
        } else if (file.getName().endsWith(".md")) {
            String content = convertMarkdown(file);
            processContentFile(content, file, outputFile, config);
        } else {
            Files.copy(file.toPath(), outputFile.toPath(), REPLACE_EXISTING);
        }
    }

    private String convertMarkdown(File file) throws IOException {
        Parser markdownParser = Parser.builder().build();
        Node markdown = markdownParser.parse(Files.readString(file.toPath(), UTF_8));
        HtmlRenderer markdownHtmlRenderer = HtmlRenderer.builder().build();
        return markdownHtmlRenderer.render(markdown);
    }

    private void processContentFile(String content, File file, File outputFile, StaticSiteExt config)
            throws IOException {
        for (Document template : findTemplateChain(file, config)) {
            content = renderTemplate(content, template);
        }

        if (outputFile.getName().endsWith(".md")) {
            outputFile = new File(outputFile.getParentFile(),
                outputFile.getName().replace(".md", ".html"));
        }

        Files.writeString(outputFile.toPath(), content, UTF_8);
    }

    /**
     * Returns a list of templates that should be applied to the specified file,
     * with the order of the list matching the order in which the templates
     * should be applied. If there are no templates defined, the list will be
     * empty, and the file's content should be used verbatim.
     */
    private List<Document> findTemplateChain(File file, StaticSiteExt config) {
        List<File> parentChain = new ArrayList<>();
        File current = file.getParentFile();

        while (current != null) {
            parentChain.add(current);
            current = current.getParentFile();
        }

        return parentChain.stream()
            .map(dir -> new File(dir, config.getTemplateFileName()))
            .filter(templateCache::containsKey)
            .distinct()
            .map(templateFile -> templateCache.get(templateFile).clone())
            .toList();
    }

    private String renderTemplate(String content, Document template) {
        for (String tagName : TEMPLATE_TAGS) {
            for (Element element : template.select(tagName)) {
                element.before(content);
                element.remove();
            }
        }

        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset(UTF_8);
        settings.indentAmount(4);
        template.outputSettings(settings);
        return template.html();
    }

    private boolean isIgnored(File file) {
        String path = file.getAbsolutePath();
        return path.contains(".git") || path.contains("userHome") || path.contains(".DS_Store");
    }

    private boolean isTemplateFile(File file, StaticSiteExt config) {
        return file.getName().equals(config.getTemplateFileName());
    }
}
