//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.icon;

import nl.colorize.gradle.application.ApplicationPlugin;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GenerateAppIconsTaskTest {

    private File outputDir;
    private AppIconExt config;
    private GenerateAppIconsTask task;

    @BeforeEach
    public void before(@TempDir File inputDir, @TempDir File outputDir) {
        this.outputDir = outputDir;

        Project project = ProjectBuilder.builder().withProjectDir(inputDir).build();
        project.getLayout().getBuildDirectory().set(outputDir);

        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);

        config = project.getExtensions().getByType(AppIconExt.class);
        task = (GenerateAppIconsTask) project.getTasks().getByName("generateAppIcons");
    }

    @Test
    void generateBrowserIcons() {
        config.getOriginal().set(new File("resources/icon.png").getAbsolutePath());
        task.run(config);

        assertTrue(new File(outputDir, "icons").exists());
        assertTrue(new File(outputDir, "icons/favicon.png").exists());
        assertTrue(new File(outputDir, "icons/apple-favicon.png").exists());
        assertTrue(new File(outputDir, "icons/icon-512.png").exists());
        assertTrue(new File(outputDir, "icons/icon-192.png").exists());
    }
}
