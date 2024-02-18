//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.xcode;

import nl.colorize.gradle.application.AppHelper;
import nl.colorize.gradle.application.ApplicationPlugin;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XcodeGenTaskTest {

    @Test
    void generateSpecFile(@TempDir File tempDir) throws IOException {
        XcodeGenExt config = new XcodeGenExt();
        config.setAppId("example");
        config.setBundleId("com.example");
        config.setAppName("Example App");
        config.setAppVersion("1.0");
        config.setIcon("resources/icon.png");

        XcodeGenTask task = prepareTask(tempDir);
        File specFile = new File(tempDir, "spec.yml");
        task.generateSpecFile(config, specFile);

        String expected = """
            name: "Example App"
            options:
              createIntermediateGroups: true
            targets:
              example:
                type: application
                platform: iOS
                deploymentTarget: "14.0"
                sources:
                  - example
                  - path: HybridResources
                    type: folder
                info:
                  path: "example/Info.plist"
                  properties:
                    CFBundleDisplayName: "Example App"
                    CFBundleShortVersionString: $(MARKETING_VERSION)
                    CFBundleVersion: $(CURRENT_PROJECT_VERSION)
                    UILaunchScreen:
                      UIColorName: "#000000"
                    UISupportedInterfaceOrientations~ipad:
                      - UIInterfaceOrientationPortrait
                      - UIInterfaceOrientationPortraitUpsideDown
                      - UIInterfaceOrientationLandscapeLeft
                      - UIInterfaceOrientationLandscapeRight
                settings:
                  PRODUCT_BUNDLE_IDENTIFIER: com.example
                  ASSETCATALOG_COMPILER_APPICON_NAME: AppIcon
                  TARGETED_DEVICE_FAMILY: 1,2
                  PRODUCT_NAME: "Example App"
                  INFOPLIST_KEY_CFBundleDisplayName: "Example App"
                  CURRENT_PROJECT_VERSION: "1.0"
                  MARKETING_VERSION: "1.0"
            """;

        assertEquals(expected, Files.readString(specFile.toPath(), UTF_8));
    }

    @Test
    void generateProjectStructure(@TempDir File tempDir) throws IOException {
        AppHelper.mkdir(new File(tempDir, "resources"));

        XcodeGenExt config = new XcodeGenExt();
        config.setAppId("example");
        config.setBundleId("com.example");
        config.setAppName("Example App");
        config.setAppVersion("1.0");
        config.setIcon(new File("resources/icon.png").getAbsolutePath());
        config.setResourcesDir("resources");

        XcodeGenTask task = prepareTask(tempDir);
        task.generateProjectStructure(config, tempDir);

        assertTrue(new File(tempDir, "example").exists());
        assertTrue(new File(tempDir, "HybridResources").exists());
    }

    @Test
    void generateAppIcons(@TempDir File tempDir) throws IOException {
        AppHelper.mkdir(new File(tempDir, "resources"));

        XcodeGenExt config = new XcodeGenExt();
        config.setAppId("example");
        config.setBundleId("com.example");
        config.setAppName("Example App");
        config.setAppVersion("1.0");
        config.setIcon(new File("resources/icon.png").getAbsolutePath());
        config.setResourcesDir("resources");

        XcodeGenTask task = prepareTask(tempDir);
        task.generateProjectStructure(config, tempDir);

        File iconDir = new File(tempDir, "example/Assets.xcassets/AppIcon.appiconset");
        File index = new File(iconDir, "Contents.json");

        String expected = """
            {
                "images" : [
                    {
                        "filename" : "icon-120.png",
                        "idiom" : "iphone",
                        "scale" : "2x",
                        "size" : "60x60"
                    },
                    {
                        "filename" : "icon-180.png",
                        "idiom" : "iphone",
                        "scale" : "3x",
                        "size" : "60x60"
                    },
                    {
                        "filename" : "icon-152.png",
                        "idiom" : "ipad",
                        "scale" : "2x",
                        "size" : "76x76"
                    },
                    {
                        "filename" : "icon-167.png",
                        "idiom" : "ipad",
                        "scale" : "2x",
                        "size" : "83.5x83.5"
                    },
                    {
                        "filename" : "icon-1024.png",
                        "idiom" : "ios-marketing",
                        "scale" : "1x",
                        "size" : "1024x1024"
                    }
                ],
                "info" : {
                    "author" : "xcode",
                    "version" : 1
                }
            }
            """;

        assertTrue(iconDir.exists());
        assertTrue(new File(iconDir, "icon-1024.png").exists());
        assertTrue(new File(iconDir, "icon-180.png").exists());
        assertTrue(new File(iconDir, "icon-167.png").exists());
        assertTrue(new File(iconDir, "icon-152.png").exists());
        assertTrue(new File(iconDir, "icon-120.png").exists());
        assertTrue(index.exists());
        assertEquals(expected, Files.readString(index.toPath(), UTF_8));
    }

    private XcodeGenTask prepareTask(File tempDir) {
        Project project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build();

        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);

        return (XcodeGenTask) project.getTasks().getByName("xcodeGen");
    }
}
