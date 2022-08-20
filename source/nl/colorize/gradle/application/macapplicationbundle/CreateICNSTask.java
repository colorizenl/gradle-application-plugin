//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.macapplicationbundle;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON;

public class CreateICNSTask extends DefaultTask {

    private File source;
    private File target;

    private static final List<Integer> SIZE_VARIANTS = List.of(16, 32, 128, 256, 512);

    @TaskAction
    public void run() {
        if (!source.getName().endsWith(".png")) {
            throw new IllegalArgumentException("Can only generate ICNS icon from PNG image");
        }

        if (!target.getName().endsWith(".icns")) {
            throw new IllegalArgumentException("Can only generate ICNS icons");
        }

        try {
            BufferedImage sourceImage = loadSourceImage();
            Map<String, BufferedImage> icons = createIcons(sourceImage);
            File iconSetDir = createIconSet(icons);
            convertIconSetToICNS(iconSetDir);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create ICNS icon", e);
        }
    }

    private BufferedImage loadSourceImage() throws IOException {
        BufferedImage image = ImageIO.read(source);
        if (image.getWidth() != image.getHeight()) {
            throw new RuntimeException("Image must be square to be used as icon");
        }
        return image;
    }

    private Map<String, BufferedImage> createIcons(BufferedImage sourceImage) {
        Map<String, BufferedImage> icons = new LinkedHashMap<String, BufferedImage>();

        for (int variant : SIZE_VARIANTS) {
            icons.put("icon_" + variant + "x" + variant + ".png", scaleImage(sourceImage, variant));
            icons.put("icon_" + variant + "x" + variant + "@2x.png", scaleImage(sourceImage, 2 * variant));
        }

        return icons;
    }

    private BufferedImage scaleImage(BufferedImage image, int size) {
        if (image.getWidth() == size && image.getHeight() == size) {
            return image;
        }

        BufferedImage result = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(image, 0, 0, size, size, null);
        g2.dispose();
        return result;
    }

    private File createIconSet(Map<String, BufferedImage> icons) throws IOException {
        File tempDir = Files.createTempDirectory("icns-" + System.currentTimeMillis()).toFile();
        File iconSetDir = new File(tempDir, "icon.iconset");
        iconSetDir.mkdir();

        for (Map.Entry<String, BufferedImage> entry : icons.entrySet()) {
            File outputFile = new File(iconSetDir, entry.getKey());
            ImageIO.write(entry.getValue(), "png", outputFile);
        }

        return iconSetDir;
    }

    private void convertIconSetToICNS(File iconSetDir) {
        getProject().exec(exec -> {
            exec.commandLine("iconutil",
                "-c", "icns",
                iconSetDir.getAbsolutePath(),
                "-o", target.getAbsolutePath());
        });
    }

    public File getSource() {
        return source;
    }

    public void setSource(File source) {
        this.source = source;
    }

    public File getTarget() {
        return target;
    }

    public void setTarget(File target) {
        this.target = target;
    }
}
