//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

/**
 * Generates icons based on the format, size, and design rules for each
 * supported platform. This is used by the standalone icon tasks, but it's
 * also used by other platform-specific tasks that need to generate icons on
 * the fly.
 */
public class IconGenerator {

    private BufferedImage baseIcon;

    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 80);
    private static final int SHADOW_OFFSET = 1;
    private static final int SHADOW_BLUR = 4;

    public IconGenerator(File baseIconFile) {
        try {
            baseIcon = ImageIO.read(baseIconFile);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read icon file: " + baseIconFile, e);
        }
    }

    public IconGenerator(BufferedImage baseIcon) {
        this.baseIcon = baseIcon;
    }

    public BufferedImage create(int size, Color background, boolean mask) {
        return generateIcon(size, background, mask);
    }

    public void save(int size, Color background, boolean mask, File outputFile) {
        try {
            BufferedImage icon = generateIcon(size, background, mask);
            ImageIO.write(icon, "png", outputFile);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to save icon: " + outputFile, e);
        }
    }

    private BufferedImage generateIcon(int size, Color background, boolean mask) {
        if (mask) {
            return generateMaskIcon(size);
        } else {
            return generateRegularIcon(size, background);
        }
    }

    private BufferedImage generateRegularIcon(int size, Color background) {
        BufferedImage image = new BufferedImage(size, size, TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
        if (background != null) {
            g2.setColor(background);
            g2.fillRect(0, 0, size, size);
        }
        g2.drawImage(progressiveScaleImage(baseIcon, size), 0, 0, null);
        g2.dispose();
        return image;
    }

    private BufferedImage generateMaskIcon(int size) {
        float factor = size / 512f;
        int inset = Math.round(50 * factor);
        int resize = Math.round(412 * factor);
        int radius = Math.round(128 * factor);

        BufferedImage image = new BufferedImage(size, size, TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(inset, inset, resize, resize, radius, radius);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, 1f));
        g2.drawImage(progressiveScaleImage(baseIcon, resize), inset, inset, null);
        g2.dispose();

        return applyDropShadow(image);
    }

    private BufferedImage progressiveScaleImage(BufferedImage original, int size) {
        BufferedImage current = original;
        int currentWidth = current.getWidth(null);
        int currentHeight = current.getHeight(null);

        while (currentWidth >= size * 2 || currentHeight >= size * 2) {
            currentWidth = currentWidth / 2;
            currentHeight = currentHeight / 2;
            current = scaleImage(current, currentWidth, currentHeight);
        }

        return scaleImage(current, size, size);
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

    private BufferedImage applyDropShadow(BufferedImage image) {
        BufferedImage shadow = new BufferedImage(image.getWidth(), image.getHeight(), TYPE_INT_ARGB);
        Graphics2D shadowG2 = shadow.createGraphics();
        shadowG2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        shadowG2.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
        shadowG2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
        shadowG2.drawImage(image, SHADOW_OFFSET, SHADOW_OFFSET, null);
        shadowG2.setComposite(AlphaComposite.SrcIn);
        shadowG2.setColor(SHADOW_COLOR);
        shadowG2.fillRect(0, 0, shadow.getWidth(), shadow.getHeight());
        shadowG2.dispose();

        BufferedImage combined = applyGaussianBlur(shadow, SHADOW_BLUR);
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
}
