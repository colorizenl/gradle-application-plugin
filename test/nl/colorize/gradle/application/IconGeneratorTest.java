//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IconGeneratorTest {

    @Test
    void generateRegularIcon() {
        IconGenerator iconGenerator = new IconGenerator(createTestIcon());
        BufferedImage result = iconGenerator.create(256, null, false);

        assertEquals(256, result.getWidth());
        assertEquals(256, result.getHeight());
        assertEquals("", toHexColor(result, 1, 1));
        assertEquals("#FF0000", toHexColor(result, 128, 128));
    }

    @Test
    void generateMaskIcon() {
        IconGenerator iconGenerator = new IconGenerator(createTestIcon());
        BufferedImage result = iconGenerator.create(256, null, true);

        assertEquals(256, result.getWidth());
        assertEquals(256, result.getHeight());
        assertEquals("", toHexColor(result, 1, 1));
        assertEquals("#FF0000", toHexColor(result, 128, 128));
    }

    @Test
    void generateBackground() {
        IconGenerator iconGenerator = new IconGenerator(createTestIcon());
        BufferedImage result = iconGenerator.create(256, Color.BLUE, false);

        assertEquals(256, result.getWidth());
        assertEquals(256, result.getHeight());
        assertEquals("#0000FF", toHexColor(result, 1, 1));
        assertEquals("#FF0000", toHexColor(result, 128, 128));
    }

    private static BufferedImage createTestIcon() {
        BufferedImage icon = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = icon.createGraphics();
        g2.setColor(Color.RED);
        g2.fillOval(0, 0, 100, 100);
        g2.dispose();
        return icon;
    }

    private String toHexColor(BufferedImage image, int x, int y) {
        int rgb = image.getRGB(x, y);
        if (rgb == 0) {
            return "";
        }
        return "#" + Integer.toHexString(rgb).substring(2, 8).toUpperCase();
    }
}
