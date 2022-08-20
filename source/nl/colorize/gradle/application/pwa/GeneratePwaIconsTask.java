//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.pwa;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON;

public class GeneratePwaIconsTask extends DefaultTask {

    @TaskAction
    public void run() {
        PwaExt config = getProject().getExtensions().getByType(PwaExt.class);
        run(config);
    }

    protected void run(PwaExt config) {
        try {
            BufferedImage icon = ImageIO.read(config.getIconFile());
            generateImageSizeVariants(icon, config);
        } catch (IOException e) {
            throw new RuntimeException("Unable to write icon file", e);
        }
    }

    private void generateImageSizeVariants(BufferedImage original, PwaExt config) throws IOException {
        File iconDir = config.getIconOutputDir();
        iconDir.mkdir();

        BufferedImage image = original;

        for (int size : config.getIconSizes()) {
            File outputFile = new File(config.getIconOutputDir(), "icon-" + size + ".png");
            if (!outputFile.exists()) {
                BufferedImage variant = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = variant.createGraphics();
                g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(image, 0, 0, size, size, null);
                g2.dispose();

                ImageIO.write(variant, "png", outputFile);

                image = variant;
            }
        }
    }
}
