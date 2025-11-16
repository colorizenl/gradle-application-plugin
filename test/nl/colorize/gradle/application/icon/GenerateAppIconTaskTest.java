//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.icon;

import nl.colorize.gradle.application.ApplicationPlugin;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GenerateAppIconTaskTest {

    @Test
    void generateBrowserIcons(@TempDir File inputDir, @TempDir File outputDir) {
        AppIconExt config = new AppIconExt();
        config.setOriginal(new File("resources/icon.png").getAbsolutePath());

        GenerateAppIconsTask task = prepare(inputDir, outputDir);
        task.run(config);

        assertTrue(new File(outputDir, "icons").exists());
        assertTrue(new File(outputDir, "icons/favicon.png").exists());
        assertTrue(new File(outputDir, "icons/apple-favicon.png").exists());
        assertTrue(new File(outputDir, "icons/icon-512.png").exists());
        assertTrue(new File(outputDir, "icons/icon-192.png").exists());
    }

    private GenerateAppIconsTask prepare(File inputDir, File outputDir) {
        Project project = ProjectBuilder.builder().withProjectDir(inputDir).build();
        project.setBuildDir(outputDir);
        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);
        return (GenerateAppIconsTask) project.getTasks().getByName("generateAppIcons");
    }
}
