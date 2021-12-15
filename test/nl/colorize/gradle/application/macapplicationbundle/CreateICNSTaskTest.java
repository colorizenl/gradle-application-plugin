//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.macapplicationbundle;

import nl.colorize.gradle.application.ApplicationPlugin;
import org.gradle.api.Project;
import org.gradle.internal.impldep.com.google.common.io.Files;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateICNSTaskTest {

    @Test
    void createICNS() throws IOException {
        BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.RED);
        g2.fillOval(0, 0, 256, 256);
        g2.dispose();

        File tempFile = File.createTempFile("image", ".png");
        ImageIO.write(image, "png", tempFile);

        File tempDir = Files.createTempDir();
        Project project = ProjectBuilder.builder().withProjectDir(tempDir).build();
        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);

        CreateICNSTask task = (CreateICNSTask) project.getTasks().getByName("createICNS");
        task.setSource(tempFile);
        task.setTarget(File.createTempFile("icon", ".icns"));
        task.run();

        assertTrue(task.getTarget().exists());
    }
}
