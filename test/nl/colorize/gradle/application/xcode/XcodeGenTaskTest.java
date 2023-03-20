//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.xcode;

import nl.colorize.gradle.application.ApplicationPlugin;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

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
            name: Example App
            targets:
              example:
                type: application
                platform: iOS
                deploymentTarget: "12.0"
                sources: [example]
                settings:
                  base:
                    INFOPLIST_FILE: example/Info.plist
                    PRODUCT_BUNDLE_IDENTIFIER: com.example
            """;

        assertEquals(expected, Files.readString(specFile.toPath(), UTF_8));
    }

    @Test
    void generateProjectStructure(@TempDir File tempDir) throws IOException {
        XcodeGenExt config = new XcodeGenExt();
        config.setAppId("example");
        config.setBundleId("com.example");
        config.setAppName("Example App");
        config.setAppVersion("1.0");
        config.setIcon("resources/icon.png");

        XcodeGenTask task = prepareTask(tempDir);
        task.generateProjectStructure(config, tempDir);

        assertTrue(new File(tempDir, "example").exists());
        assertTrue(new File(tempDir, "HybridResources").exists());
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
