//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.icon;

import nl.colorize.gradle.application.AppHelper;
import nl.colorize.gradle.application.IconGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class GenerateAppIconsTask extends DefaultTask {

    @Nested
    public abstract Property<AppIconExt> getExt();

    @Inject
    public abstract ExecOperations getExecService();

    private static final List<IconVariant> MAC_ICONS = List.of(
        new IconVariant("icon_16x16.png", 16, false, true),
        new IconVariant("icon_16x16@2x.png", 32, false, true),
        new IconVariant("icon_32x32.png", 32, false, true),
        new IconVariant("icon_32x32@2x.png", 64, false, true),
        new IconVariant("icon_128x128.png", 128, false, true),
        new IconVariant("icon_128x128@2x.png", 256, false, true),
        new IconVariant("icon_256x256.png", 256, false, true),
        new IconVariant("icon_256x256@2x.png", 512, false, true),
        new IconVariant("icon_512x512.png", 512, false, true),
        new IconVariant("icon_512x512@2x.png", 1024, false, true)
    );

    private static final List<IconVariant> IOS_ICONS = List.of(
        new IconVariant("icon-120.png", 120, true, false),
        new IconVariant("icon-152.png", 152, true, false),
        new IconVariant("icon-167.png", 167, true, false),
        new IconVariant("icon-180.png", 180, true, false),
        new IconVariant("icon-1024.png", 1024, true, false)
    );

    private static final List<IconVariant> PWA_ICONS = List.of(
        new IconVariant("icon-192.png", 192, false, true),
        new IconVariant("icon-512.png", 512, false, true)
    );

    private static final IconVariant WINDOWS_ICON = new IconVariant("icon-48.png", 48, false, true);
    private static final IconVariant FAVICON = new IconVariant("favicon.png", 32, false, false);
    private static final IconVariant SAFARI_FAVICON = new IconVariant("apple-favicon.png", 180, true, false);

    @TaskAction
    public void run() {
        run(getExt().get());
    }

    protected void run(AppIconExt config) {
        File outputDir = config.toOutputDir(config.getOutputDir().get());
        AppHelper.cleanDirectory(outputDir);

        try {
            BufferedImage original = ImageIO.read(new File(config.getOriginal().get()));
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
        generateIcon(original, SAFARI_FAVICON, outputDir);
    }

    private void generateMacIconSet(BufferedImage original, File outputDir) {
        File iconSet = AppHelper.mkdir(new File(outputDir, "icon.iconset"));

        for (IconVariant variant : MAC_ICONS) {
            generateIcon(original, variant, iconSet);
        }

        getExecService().exec(exec -> {
            exec.workingDir(outputDir);
            exec.commandLine("iconutil", "-c", "icns", "icon.iconset");
        });
    }

    private void generateAppIconSet(BufferedImage original, File outputDir) {
        File iconSet = AppHelper.mkdir(new File(outputDir, "AppIcon.appiconset"));

        for (IconVariant variant : IOS_ICONS) {
            generateIcon(original, variant, iconSet);
        }

        try {
            String metadata = AppHelper.loadResourceFile("app-icon-contents.json");
            Files.writeString(new File(iconSet, "Contents.json").toPath(), metadata, UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error while creating icon metadata file", e);
        }
    }

    private void generateIcon(BufferedImage original, IconVariant variant, File outputDir) {
        File outputFile = new File(outputDir, variant.name);
        String requestedBackground = getExt().get().getBackgroundColor().get();
        Color actualBackground = variant.background ? Color.decode(requestedBackground) : null;

        IconGenerator iconGenerator = new IconGenerator(original);
        iconGenerator.save(variant.size, actualBackground, variant.maskable, outputFile);
    }

    /**
     * Describes an icon for platforms that require application icons to
     * support multiple variants. Apple platforms use a slightly obscure
     * notation, where {@code 32x32} and {@code 16x16@2} both indicate an
     * icon that is 32x32 pixels in size.
     */
    private record IconVariant(String name, int size, boolean background, boolean maskable) {
    }
}
