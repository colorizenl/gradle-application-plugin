//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.xcode;

import nl.colorize.gradle.application.AppHelper;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.nio.charset.StandardCharsets.UTF_8;

public class XcodeGenTask extends DefaultTask {

    private static final List<IconVariant> ICON_VARIANTS = List.of(
        new IconVariant(120, "iphone", 2, "60x60"),
        new IconVariant(180, "iphone", 3, "60x60"),
        new IconVariant(152, "ipad", 2, "76x76"),
        new IconVariant(167, "ipad", 2, "83.5x83.5"),
        new IconVariant(1024, "ios-marketing", 1, "1024x1024")
    );

    @TaskAction
    public void run() {
        AppHelper.requireMac();
        XcodeGenExt ext = getProject().getExtensions().getByType(XcodeGenExt.class);
        ext.validate();
        requireXcodeGen(ext);

        try {
            File outputDir = AppHelper.getOutputDir(getProject(), ext.getOutputDir());
            generateProjectStructure(ext, outputDir);

            File specFile = new File(outputDir, "xcodegen.yml");
            generateSpecFile(ext, specFile);

            List<String> xcodeGenCommand = buildCommand(ext, specFile, outputDir);
            getProject().exec(exec -> exec.commandLine(xcodeGenCommand));
        } catch (IOException e) {
            throw new RuntimeException("Unable to generate Xcode project", e);
        }
    }

    private void requireXcodeGen(XcodeGenExt ext) {
        String path = ext.getXcodeGenPath();
        File executable = new File(path);
        if (!executable.exists()) {
            throw new UnsupportedOperationException("XcodeGen not found at " + path);
        }
    }

    protected void generateProjectStructure(XcodeGenExt ext, File outputDir) throws IOException {
        File appDir = AppHelper.mkdir(new File(outputDir, ext.getAppId()));

        File resourcesDir = AppHelper.mkdir(new File(outputDir, "HybridResources"));
        AppHelper.cleanDirectory(resourcesDir);
        copyResources(ext, resourcesDir);

        String swiftCode = AppHelper.loadResourceFile("App.swift");
        Files.writeString(new File(appDir, "App.swift").toPath(), swiftCode, UTF_8);

        File assetsDir = AppHelper.mkdir(new File(appDir, "Assets.xcassets"));
        Files.writeString(new File(assetsDir, "Contents.json").toPath(),
            AppHelper.loadResourceFile("empty-contents.json"), UTF_8);
        File iconDir = AppHelper.mkdir(new File(assetsDir, "AppIcon.appiconset"));
        File icon = AppHelper.getProjectFile(getProject(), ext.getIcon());
        generateIconSet(icon, iconDir, Color.decode(ext.getIconBackgroundColor()));
    }

    protected void generateSpecFile(XcodeGenExt ext, File specFile) {
        Map<String, String> properties = Map.of(
            "{{appName}}", ext.getAppName(),
            "{{appId}}", ext.getAppId(),
            "{{deploymentTarget}}", ext.getDeploymentTarget(),
            "{{launchScreenColor}}", ext.getLaunchScreenColor(),
            "{{bundleId}}", ext.getBundleId(),
            "{{appVersion}}", ext.getBundleVersion(),
            "{{buildVersion}}", ext.getBuildVersion()
        );

        try {
            String template = AppHelper.rewriteTemplate("xcodegen-template.yml", properties);
            Files.writeString(specFile.toPath(), template, UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error while generating XcodeGen spec file", e);
        }
    }

    private void copyResources(XcodeGenExt ext, File outputDir) {
        File inputDir = AppHelper.getProjectDir(getProject(), ext.getResourcesDir());

        getProject().copy(copy -> {
            copy.from(inputDir);
            copy.into(outputDir);
        });
    }

    private void generateIconSet(File baseIconFile, File iconDir, Color background) throws IOException {
        String metadata = AppHelper.loadResourceFile("app-icon-contents.json");
        Files.writeString(new File(iconDir, "Contents.json").toPath(), metadata, UTF_8);

        BufferedImage base = ImageIO.read(baseIconFile);

        for (IconVariant variant : ICON_VARIANTS) {
            BufferedImage image = new BufferedImage(variant.size, variant.size, TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
            g2.setColor(background);
            g2.fillRect(0, 0, variant.size, variant.size);
            g2.drawImage(scaleImage(base, variant.size, variant.size, true), 0, 0, null);
            g2.dispose();

            File outputFile = new File(iconDir, "icon-" + variant.size + ".png");
            ImageIO.write(image, "png", outputFile);
        }
    }

    private BufferedImage scaleImage(Image original, int width, int height, boolean highQuality) {
        Image current = original;
        int currentWidth = current.getWidth(null);
        int currentHeight = current.getHeight(null);

        while (highQuality && (currentWidth >= width * 2 || currentHeight >= height * 2)) {
            currentWidth = currentWidth / 2;
            currentHeight = currentHeight / 2;
            current = scaleImage(current, currentWidth, currentHeight);
        }

        return scaleImage(current, width, height);
    }

    private BufferedImage scaleImage(Image original, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();
        return result;
    }

    private List<String> buildCommand(XcodeGenExt ext, File specFile, File outputDir) {
        return List.of(
            ext.getXcodeGenPath(),
            "--spec", specFile.getAbsolutePath(),
            "--project", outputDir.getAbsolutePath()
        );
    }

    private record IconVariant(int size, String idiom, int scale, String description) {
    }
}
