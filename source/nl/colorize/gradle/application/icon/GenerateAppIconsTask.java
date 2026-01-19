//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.icon;

import nl.colorize.gradle.application.AppHelper;
import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class GenerateAppIconsTask extends DefaultTask {

    private ExecOperations execService;

    private static final List<IconVariant> MAC_ICONS = List.of(
        new IconVariant("icon_16x16.png", 16, true),
        new IconVariant("icon_16x16@2x.png", 32, true),
        new IconVariant("icon_32x32.png", 32, true),
        new IconVariant("icon_32x32@2x.png", 64, true),
        new IconVariant("icon_128x128.png", 128, true),
        new IconVariant("icon_128x128@2x.png", 256, true),
        new IconVariant("icon_256x256.png", 256, true),
        new IconVariant("icon_256x256@2x.png", 512, true),
        new IconVariant("icon_512x512.png", 512, true),
        new IconVariant("icon_512x512@2x.png", 1024, true)
    );

    private static final List<IconVariant> IOS_ICONS = List.of(
        new IconVariant("icon-120.png", 120, false),
        new IconVariant("icon-152.png", 152, false),
        new IconVariant("icon-167.png", 167, false),
        new IconVariant("icon-180.png", 180, false),
        new IconVariant("icon-1024.png", 1024, false)
    );

    private static final List<IconVariant> PWA_ICONS = List.of(
        new IconVariant("icon-192.png", 192, true),
        new IconVariant("icon-512.png", 512, true)
    );

    private static final IconVariant WINDOWS_ICON = new IconVariant("icon-48.png", 48, true);
    private static final IconVariant FAVICON = new IconVariant("favicon.png", 32, false);
    private static final IconVariant APPLE_FAVICON = new IconVariant("apple-favicon.png", 180, false);

    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 80);
    private static final int SHADOW_OFFSET = 1;
    private static final int SHADOW_BLUR = 4;

    @Inject
    public GenerateAppIconsTask(ExecOperations execService) {
        this.execService = execService;
    }

    @TaskAction
    public void run() {
        ExtensionContainer ext = getProject().getExtensions();
        AppIconExt config = ext.getByType(AppIconExt.class);
        run(config);
    }

    protected void run(AppIconExt config) {
        File outputDir = AppHelper.getOutputDir(getProject(), config.getOutputDir());
        AppHelper.cleanDirectory(outputDir);

        try {
            BufferedImage original = ImageIO.read(new File(config.getOriginal()));
            generateIcons(original, outputDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate icon", e);
        }
    }

    private void generateIcons(BufferedImage original, File outputDir) throws IOException {
        if (AppHelper.isMac()) {
            generateMacIconSet(original, outputDir);
        }
        generateAppIconSet(original, outputDir);
        for (IconVariant pwaIcon : PWA_ICONS) {
            generateIcon(original, pwaIcon, outputDir);
        }
        generateIcon(original, WINDOWS_ICON, outputDir);
        generateIcon(original, FAVICON, outputDir);
        generateIcon(original, APPLE_FAVICON, outputDir);
    }

    private void generateMacIconSet(BufferedImage original, File outputDir) throws IOException {
        File iconSet = new File(outputDir, "icon.iconset");
        iconSet.mkdir();

        for (IconVariant variant : MAC_ICONS) {
            BufferedImage image = generateIconVariant(original, variant);
            ImageIO.write(image, "png", new File(iconSet, variant.name));
        }

        execService.exec(exec -> {
            exec.workingDir(outputDir);
            exec.commandLine("iconutil", "-c", "icns", "icon.iconset");
        });
    }

    private void generateAppIconSet(BufferedImage original, File outputDir) throws IOException {
        File iconSet = new File(outputDir, "AppIcon.appiconset");
        iconSet.mkdir();

        for (IconVariant variant : IOS_ICONS) {
            BufferedImage image = generateIconVariant(original, variant);
            ImageIO.write(image, "png", new File(iconSet, variant.name));
        }
    }

    private BufferedImage generateIconVariant(BufferedImage original, IconVariant variant) {
        if (variant.maskable) {
            return generateMaskIcon(original, variant);
        } else {
            return generateRegularIcon(original, variant);
        }
    }

    private BufferedImage generateRegularIcon(BufferedImage original, IconVariant variant) {
        return progressiveScaleImage(original, variant.size, variant.size);
    }

    private BufferedImage generateMaskIcon(BufferedImage original, IconVariant variant) {
        float factor = variant.size / 512f;
        int inset = Math.round(50 * factor);
        int size = Math.round(412 * factor);
        int radius = Math.round(128 * factor);

        BufferedImage image = new BufferedImage(variant.size, variant.size, TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(inset, inset, size, size, radius, radius);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, 1f));
        g2.drawImage(progressiveScaleImage(original, size, size), inset, inset, null);
        g2.dispose();

        return applyDropShadow(image, SHADOW_COLOR, SHADOW_OFFSET, SHADOW_BLUR);
    }

    private void generateIcon(BufferedImage original, IconVariant variant, File outputDir)
            throws IOException {
        BufferedImage image = generateIconVariant(original, variant);
        ImageIO.write(image, "png", new File(outputDir, variant.name));
    }

    private BufferedImage progressiveScaleImage(BufferedImage original, int width, int height) {
        BufferedImage current = original;
        int currentWidth = current.getWidth(null);
        int currentHeight = current.getHeight(null);

        while (currentWidth >= width * 2 || currentHeight >= height * 2) {
            currentWidth = currentWidth / 2;
            currentHeight = currentHeight / 2;
            current = scaleImage(current, currentWidth, currentHeight);
        }

        return scaleImage(current, width, height);
    }

    private BufferedImage scaleImage(BufferedImage original, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();
        return result;
    }

    private BufferedImage applyDropShadow(BufferedImage image, Color color, int size, int blur) {
        BufferedImage shadow = new BufferedImage(image.getWidth(), image.getHeight(), TYPE_INT_ARGB);
        Graphics2D shadowG2 = shadow.createGraphics();
        shadowG2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        shadowG2.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
        shadowG2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
        shadowG2.drawImage(image, size, size, null);
        shadowG2.setComposite(AlphaComposite.SrcIn);
        shadowG2.setColor(color);
        shadowG2.fillRect(0, 0, shadow.getWidth(), shadow.getHeight());
        shadowG2.dispose();

        BufferedImage combined = applyGaussianBlur(shadow, blur);
        Graphics2D combinedG2 = combined.createGraphics();
        combinedG2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        combinedG2.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
        combinedG2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
        combinedG2.drawImage(image, 0, 0, null);
        combinedG2.dispose();
        return combined;
    }

    private BufferedImage applyGaussianBlur(BufferedImage original, int amount) {
        int size = amount * 2 + 1;
        float[] data = calculateGaussianBlurData(amount, size);
        ConvolveOp horizontal = new ConvolveOp(new Kernel(size, 1, data), ConvolveOp.EDGE_NO_OP, null);
        ConvolveOp vertical = new ConvolveOp(new Kernel(1, size, data), ConvolveOp.EDGE_NO_OP, null);

        BufferedImage blurredImage = new BufferedImage(original.getWidth(), original.getHeight(),
            TYPE_INT_ARGB);
        blurredImage = horizontal.filter(original, blurredImage);
        blurredImage = vertical.filter(blurredImage, null);
        return blurredImage;
    }

    private float[] calculateGaussianBlurData(int amount, int size) {
        float[] data = new float[size];
        float sigma = amount / 3f;
        float sigmaTwoSquared = 2f * sigma * sigma;
        float sigmaRoot = (float) Math.sqrt(sigmaTwoSquared * Math.PI);
        float total = 0f;

        for (int i = -amount; i <= amount; i++) {
            float distance = i * i;
            int index = i + amount;
            data[index] = (float) Math.exp(-distance / sigmaTwoSquared / sigmaRoot);
            total += data[index];
        }

        for (int i = 0; i < data.length; i++) {
            data[i] /= total;
        }

        return data;
    }

    /**
     * Describes an icon for platforms that require application icons to
     * support multiple variants. Apple platforms use a slightly obscure
     * notation, where {@code 32x32} and {@code 16x16@2} both indicate an
     * icon that is 32x32 pixels in size.
     */
    private record IconVariant(String name, int size, boolean maskable) {
    }
}
