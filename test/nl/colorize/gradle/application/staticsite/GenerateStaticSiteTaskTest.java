//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.staticsite;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenerateStaticSiteTaskTest {

    @Test
    void renderTemplateHTML(@TempDir File inputDir, @TempDir File outputDir) throws IOException {
        createFile(inputDir, "template.html", "<html><clrz-content></clrz-content></html>");
        createFile(inputDir, "a.html", "<div>Hello world</div>");

        StaticSiteExt config = new StaticSiteExt();
        config.setContentDir(".");

        GenerateStaticSiteTask task = prepare(inputDir, outputDir);
        task.run(config);

        String html = """
            <html>
                <head></head>
                <body>
                    <div>Hello world</div>
                </body>
            </html>""";

        assertEquals(html, readFile(outputDir, "a.html"));
        assertFalse(doesFileExist(outputDir, "template.html"));
    }

    @Test
    void templateWithClosingTag(@TempDir File inputDir, @TempDir File outputDir) throws IOException {
        createFile(inputDir, "template.html", "<html><clrz-content></clrz-content></html>");
        createFile(inputDir, "a.html", "<div>Hello world</div>");

        StaticSiteExt config = new StaticSiteExt();
        config.setContentDir(".");

        GenerateStaticSiteTask task = prepare(inputDir, outputDir);
        task.run(config);

        String html = """
            <html>
                <head></head>
                <body>
                    <div>Hello world</div>
                </body>
            </html>""";

        assertEquals(html, readFile(outputDir, "a.html"));
        assertFalse(doesFileExist(outputDir, "template.html"));
    }

    @Test
    void renderTemplateInSameLocation(@TempDir File inputDir, @TempDir File outputDir) throws IOException {
        createFile(inputDir, "template.html",
            "<html><h1>1</h1><clrz-content></clrz-content><h2>2</h2></html>");
        createFile(inputDir, "a.html", "<div>Hello world</div>");

        StaticSiteExt config = new StaticSiteExt();
        config.setContentDir(".");

        GenerateStaticSiteTask task = prepare(inputDir, outputDir);
        task.run(config);

        String html = """
            <html>
                <head></head>
                <body>
                    <h1>1</h1>
                    <div>Hello world</div>
                    <h2>2</h2>
                </body>
            </html>""";

        assertEquals(html, readFile(outputDir, "a.html"));
        assertFalse(doesFileExist(outputDir, "template.html"));
    }

    @Test
    void retainDirectoryStructure(@TempDir File inputDir, @TempDir File outputDir) throws IOException {
        createFile(inputDir, "template.html", "<html><clrz-content></clrz-content></html>");
        createFile(inputDir, "a.html", "<div>Hello world</div>");
        new File(inputDir, "b").mkdir();
        createFile(inputDir, "b/b.html", "<div>Hello world</div>");

        StaticSiteExt config = new StaticSiteExt();
        config.setContentDir(".");

        GenerateStaticSiteTask task = prepare(inputDir, outputDir);
        task.run(config);

        assertTrue(doesFileExist(outputDir, "a.html"));
        assertFalse(doesFileExist(outputDir, "b.html"));
        assertTrue(doesFileExist(outputDir, "b"));
        assertTrue(doesFileExist(outputDir, "b/b.html"));
    }

    @Test
    void renderMarkdownInTemplate(@TempDir File inputDir, @TempDir File outputDir) throws IOException {
        createFile(inputDir, "template.html", "<html><clrz-content></clrz-content></html>");
        createFile(inputDir, "a.md", "# Test\n\ntest");

        StaticSiteExt config = new StaticSiteExt();
        config.setContentDir(".");

        GenerateStaticSiteTask task = prepare(inputDir, outputDir);
        task.run(config);

        String html = """
            <html>
                <head></head>
                <body>
                    <h1>Test</h1>
                    <p>test</p>
                </body>
            </html>""";

        assertEquals(html, readFile(outputDir, "a.html"));
    }

    @Test
    void copyNonArticleFiles(@TempDir File inputDir, @TempDir File outputDir) throws IOException {
        createFile(inputDir, "template.html", "<html><clrz-content></clrz-content></html>");
        createFile(inputDir, "a.html", "<div>Hello world</div>");
        createFile(inputDir, "b.txt", "test");

        StaticSiteExt config = new StaticSiteExt();
        config.setContentDir(".");

        GenerateStaticSiteTask task = prepare(inputDir, outputDir);
        task.run(config);

        assertTrue(doesFileExist(outputDir, "b.txt"));
    }

    @Test
    void renderTemplateRecursively(@TempDir File inputDir, @TempDir File outputDir) throws IOException {
        createFile(inputDir, "template.html", "<html><clrz-content></clrz-content></html>");
        createFile(inputDir, "a.html", "<div>Hello world</div>");
        new File(inputDir, "b").mkdir();
        createFile(inputDir, "b/template.html", "<h1>Test</h1><clrz-content></clrz-content>");
        createFile(inputDir, "b/b.html", "<em>nested</em>");

        StaticSiteExt config = new StaticSiteExt();
        config.setContentDir(".");

        GenerateStaticSiteTask task = prepare(inputDir, outputDir);
        task.run(config);

        String html = """
            <html>
                <head></head>
                <body>\s
                    <h1>Test</h1>
                    <em>nested</em>\s
                </body>
            </html>""";

        assertEquals(html, readFile(outputDir, "b/b.html"));
    }

    @Test
    void selfClosingTagIsNoLongerAllowed(@TempDir File inputDir, @TempDir File outputDir) throws IOException {
        createFile(inputDir, "template.html", "<html><clrz-content /></html>");
        createFile(inputDir, "a.html", "<div>Hello world</div>");

        StaticSiteExt config = new StaticSiteExt();
        config.setContentDir(".");

        GenerateStaticSiteTask task = prepare(inputDir, outputDir);

        assertThrows(IllegalStateException.class, () -> task.run(config));
    }

    private GenerateStaticSiteTask prepare(File inputDir, File outputDir) {
        Project project = ProjectBuilder.builder().withProjectDir(inputDir).build();
        project.setBuildDir(outputDir);
        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);

        return (GenerateStaticSiteTask) project.getTasks().getByName("generateStaticSite");
    }

    private void createFile(File inputDir, String filePath, String content) throws IOException {
        File file = new File(inputDir.getAbsolutePath() + "/" + filePath);
        Files.writeString(file.toPath(), content, UTF_8);
    }

    private String readFile(File outputDir, String filePath) throws IOException {
        File file = new File(outputDir.getAbsolutePath() + "/staticsite/" + filePath);
        if (!file.exists()) {
            throw new IllegalStateException("No such file: " + file.getAbsolutePath());
        }
        return Files.readString(file.toPath(), UTF_8);
    }

    private boolean doesFileExist(File outputDir, String filePath) {
        File file = new File(outputDir.getAbsolutePath() + "/staticsite/" + filePath);
        return file.exists();
    }
}
