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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;

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
        try (PrintWriter writer = new PrintWriter(specFile, UTF_8)) {
            writer.println("name: " + ext.getAppName());
            writer.println("options:");
            writer.println("  createIntermediateGroups: true");
            writer.println("targets:");
            writer.println("  " + ext.getAppId() + ":");
            writer.println("    type: application");
            writer.println("    platform: iOS");
            writer.println("    deploymentTarget: \"" + ext.getDeploymentTarget() + "\"");
            writer.println("    sources:");
            writer.println("      - " + ext.getAppId());
            writer.println("      - path: HybridResources");
            writer.println("        type: folder");
            writer.println("    info:");
            writer.println("      path: \"" + ext.getAppId() + "/Info.plist\"");
            writer.println("      properties:");
            writer.println("        CFBundleDisplayName: \"" + ext.getAppName() + "\"");
            writer.println("        CFBundleShortVersionString: \"" + ext.getAppVersion() + "\"");
            writer.println("        CFBundleVersion: \"" + ext.getBuildVersion() + "\"");
            writer.println("        UILaunchScreen:");
            writer.println("          UIColorName: " + ext.getLaunchScreenColor());
            writer.println("        UISupportedInterfaceOrientations~ipad:");
            writer.println("          - UIInterfaceOrientationPortrait");
            writer.println("          - UIInterfaceOrientationPortraitUpsideDown");
            writer.println("          - UIInterfaceOrientationLandscapeLeft");
            writer.println("          - UIInterfaceOrientationLandscapeRight");
            writer.println("    settings:");
            writer.println("      PRODUCT_BUNDLE_IDENTIFIER: " + ext.getBundleId());
            writer.println("      ASSETCATALOG_COMPILER_APPICON_NAME: AppIcon");
            writer.println("      TARGETED_DEVICE_FAMILY: 1,2");
            writer.println("      PRODUCT_NAME: \"" + ext.getAppName() + "\"");
            writer.println("      INFOPLIST_KEY_CFBundleDisplayName: \"" + ext.getAppName() + "\"");
            writer.println("      CURRENT_PROJECT_VERSION: \"" + ext.getBuildVersion() + "\"");
            writer.println("      MARKETING_VERSION: \"" + ext.getAppVersion() + "\"");
        } catch (IOException e) {
            throw new RuntimeException("Unable to generate XcodeGen spec file", e);
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
            g2.setColor(background);
            g2.fillRect(0, 0, image.getWidth(), image.getHeight());
            g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(base, 0, 0, image.getWidth(), image.getHeight(), null);
            g2.dispose();

            File outputFile = new File(iconDir, "icon-" + variant.size + ".png");
            ImageIO.write(image, "png", outputFile);
        }
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
