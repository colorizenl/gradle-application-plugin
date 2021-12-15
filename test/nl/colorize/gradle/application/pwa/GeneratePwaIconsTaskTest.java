//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.pwa;

import nl.colorize.gradle.application.ApplicationPlugin;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratePwaIconsTaskTest {

    @Test
    void generateIcons(@TempDir File tempDir) throws IOException {
        Project project = ProjectBuilder.builder().withProjectDir(tempDir).build();
        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);

        File iconFile = new File(tempDir, "icon.png");
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("icon.png")) {
            Files.write(iconFile.toPath(), stream.readAllBytes());
        }

        PwaExt config = new PwaExt();
        config.setIconFile(iconFile);
        config.setIconOutputDir(tempDir);

        ((GeneratePwaIconsTask) project.getTasks().getByName("generatePwaIcons")).run(config);

        assertTrue(iconFile.exists());
        assertEquals(1024, ImageIO.read(iconFile).getWidth());

        assertTrue(new File(tempDir, "icon-48.png").exists());
        assertEquals(48, ImageIO.read(new File(tempDir, "icon-48.png")).getWidth());

        assertTrue(new File(tempDir, "icon-192.png").exists());
        assertEquals(192, ImageIO.read(new File(tempDir, "icon-192.png")).getWidth());
    }
}
