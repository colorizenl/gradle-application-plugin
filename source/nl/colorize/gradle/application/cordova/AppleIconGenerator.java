//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.cordova;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AppleIconGenerator {

    private static final List<Integer> IOS_VARIANTS = List.of(60, 76, 1024);

    public void generateIconSet(File original, File location) throws IOException {
        BufferedImage sourceImage = loadIcon(original);
        List<Icon> iconSet = toIconSet(sourceImage, IOS_VARIANTS);

        location.mkdir();
        saveIconSet(iconSet, location);
        generateContentsJSON(iconSet, new File(location, "Contents.json"));
    }

    private BufferedImage loadIcon(File location) throws IOException {
        BufferedImage image = ImageIO.read(location);
        if (image.getWidth() != image.getHeight()) {
            throw new RuntimeException("Image must be square to be used as icon");
        }
        return image;
    }

    private List<Icon> toIconSet(BufferedImage original, List<Integer> variants) {
        List<Icon> iconSet = new ArrayList<>();

        for (int variant : variants) {
            iconSet.add(new Icon(getIconName(variant, 1), 1, scaleIconImage(original, variant)));
            if (variant < 1024) {
                iconSet.add(new Icon(getIconName(variant, 2), 2, scaleIconImage(original, 2 * variant)));
            }
            if (variant == 60) {
                iconSet.add(new Icon(getIconName(variant, 3), 3, scaleIconImage(original, 3 * variant)));
            }
        }

        return iconSet;
    }

    private String getIconName(int size, int factor) {
        if (factor == 1) {
            return "icon-" + size + "x" + size + ".png";
        }
        return "icon-" + size + "x" + size + "@" + factor + "x.png";
    }

    private BufferedImage scaleIconImage(BufferedImage original, int size) {
        if (original.getWidth() == size && original.getHeight() == size) {
            return original;
        }

        BufferedImage rescaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = rescaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(original, 0, 0, size, size, null);
        g2.dispose();
        return rescaled;
    }

    private void saveIconSet(List<Icon> iconSet, File dir) throws IOException {
        for (Icon icon : iconSet) {
            File imageFile = new File(dir, icon.name);
            ImageIO.write(icon.image, "png", imageFile);
        }
    }

    private void generateContentsJSON(List<Icon> iconSet, File outputFile) {
        try (PrintWriter writer = new PrintWriter(outputFile, StandardCharsets.UTF_8)) {
            writer.println("{");
            writer.println("  \"images\" : [");
            for (Icon icon : iconSet) {
                writer.println("    {");
                writer.println("      \"size\" : \"" + icon.getSize() + "\",");
                writer.println("      \"idiom\" : \"" + icon.getIdiom() + "\",");
                writer.println("      \"filename\" : \"" + icon.name + "\",");
                writer.println("      \"scale\" : \"" + icon.variant + "x\"");
                writer.println(iconSet.get(iconSet.size() - 1).equals(icon) ? "    }" : "    },");
            }
            writer.println("  ],");
            writer.println("  \"info\" : {");
            writer.println("    \"version\" : 1,");
            writer.println("    \"author\" : \"xcode\"");
            writer.println("  }");
            writer.println("}");
        } catch (IOException e) {
            throw new RuntimeException("Unable to generate " + outputFile.getAbsolutePath());
        }
    }

    /**
     * Represents all properties corresponding to one of the icon images within
     * an Apple icon.
     */
    private static class Icon {

        private String name;
        private int variant;
        private BufferedImage image;

        public Icon(String name, int variant, BufferedImage image) {
            this.name = name;
            this.variant = variant;
            this.image = image;
        }

        public String getSize() {
            int size = image.getWidth() / variant;
            return size + "x" + size;
        }

        public String getIdiom() {
            if (image.getWidth() == 1024) {
                return "ios-marketing";
            } else if (image.getWidth() % 76 == 0) {
                return "ipad";
            } else {
                return "iphone";
            }
        }
    }
}
