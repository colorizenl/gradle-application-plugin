//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.xcode;

import nl.colorize.gradle.application.AppHelper;
import nl.colorize.gradle.application.IconGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class XcodeGenTask extends DefaultTask {

    @Nested
    public abstract Property<XcodeGenExt> getExt();

    @Inject
    public abstract ExecOperations getExecService();

    @Inject
    public abstract FileSystemOperations getFileSystemOperations();
    
    private static final List<String> SWIFT_FILES = List.of(
        "App.swift",
        "ScriptBridge.swift"
    );

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
        XcodeGenExt ext = getExt().get();
        requireXcodeGen(ext);

        try {
            File outputDir = ext.toOutputDir(ext.getOutputDir().get());
            generateProjectStructure(ext, outputDir);

            File specFile = new File(outputDir, "xcodegen.yml");
            generateSpecFile(ext, specFile);

            List<String> xcodeGenCommand = buildCommand(ext, specFile, outputDir);
            getExecService().exec(exec -> exec.commandLine(xcodeGenCommand));
        } catch (IOException e) {
            throw new RuntimeException("Unable to generate Xcode project", e);
        }
    }

    private void requireXcodeGen(XcodeGenExt ext) {
        String path = ext.getXcodeGenPath().get();
        File executable = new File(path);
        if (!executable.exists()) {
            throw new UnsupportedOperationException("XcodeGen not found at " + path);
        }
    }

    protected void generateProjectStructure(XcodeGenExt ext, File outputDir) throws IOException {
        File appDir = AppHelper.mkdir(new File(outputDir, ext.getAppId().get()));

        File resourcesDir = AppHelper.mkdir(new File(outputDir, "HybridResources"));
        AppHelper.cleanDirectory(resourcesDir);
        copyResources(ext, resourcesDir);

        for (String swiftFile : SWIFT_FILES) {
            String swiftCode = AppHelper.loadResourceFile(swiftFile);
            Files.writeString(new File(appDir, swiftFile).toPath(), swiftCode, UTF_8);
        }

        File assetsDir = AppHelper.mkdir(new File(appDir, "Assets.xcassets"));
        Files.writeString(new File(assetsDir, "Contents.json").toPath(),
            AppHelper.loadResourceFile("empty-contents.json"), UTF_8);
        File iconDir = AppHelper.mkdir(new File(assetsDir, "AppIcon.appiconset"));
        File icon = ext.toProjectFile(ext.getIcon().get());
        generateIconSet(icon, iconDir, Color.decode(ext.getIconBackgroundColor().get()));
    }

    protected void generateSpecFile(XcodeGenExt ext, File specFile) {
        Map<String, String> properties = Map.of(
            "{{appName}}", ext.getAppName().get(),
            "{{appId}}", ext.getAppId().get(),
            "{{deploymentTarget}}", ext.getDeploymentTarget().get(),
            "{{launchScreenColor}}", ext.getLaunchScreenColor().get(),
            "{{bundleId}}", ext.getBundleId().get(),
            "{{appVersion}}", ext.getBundleVersion().get(),
            "{{buildVersion}}", getBuildVersion()
        );

        try {
            String template = AppHelper.rewriteTemplate("xcodegen-template-ios.yml", properties);
            Files.writeString(specFile.toPath(), template, UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error while generating XcodeGen spec file", e);
        }
    }

    private void copyResources(XcodeGenExt ext, File outputDir) {
        getFileSystemOperations().copy(copy -> {
            copy.from(ext.toProjectFile(ext.getResourcesDir().get()));
            copy.into(outputDir);
        });
    }

    private void generateIconSet(File baseIconFile, File iconDir, Color background) throws IOException {
        String metadata = AppHelper.loadResourceFile("app-icon-contents.json");
        Files.writeString(new File(iconDir, "Contents.json").toPath(), metadata, UTF_8);

        IconGenerator iconGenerator = new IconGenerator(baseIconFile);
        for (IconVariant variant : ICON_VARIANTS) {
            File outputFile = new File(iconDir, "icon-" + variant.size + ".png");
            iconGenerator.save(variant.size, background, false, outputFile);
        }
    }

    private List<String> buildCommand(XcodeGenExt ext, File specFile, File outputDir) {
        return List.of(
            ext.getXcodeGenPath().get(),
            "--spec", specFile.getAbsolutePath(),
            "--project", outputDir.getAbsolutePath()
        );
    }

    private String getBuildVersion() {
        String bundleVersion = getExt().get().getBundleVersion().get();
        return System.getProperty("buildversion", bundleVersion);
    }

    private record IconVariant(int size, String idiom, int scale, String description) {
    }
}
