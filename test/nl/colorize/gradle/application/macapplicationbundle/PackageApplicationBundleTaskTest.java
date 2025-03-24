//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.macapplicationbundle;

import nl.colorize.gradle.application.ApplicationPlugin;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PackageApplicationBundleTaskTest {

    @Test
    void runJPackage(@TempDir File tempDir) {
        Project project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build();

        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);

        project.copy(copy -> {
            copy.from(new File("resources").getAbsolutePath());
            copy.into(new File(tempDir, "resources").getAbsolutePath());
        });

        MacApplicationBundleExt config = new MacApplicationBundleExt();
        config.setName("Example");
        config.setIdentifier("com.example");
        config.setMainJarName("example.jar");
        config.setMainClassName("HelloWorld.Main");
        config.setContentDir("resources");
        config.setDescription("?");

        PackageApplicationBundleTask task = (PackageApplicationBundleTask) project.getTasks()
            .getByName("packageApplicationBundle");
        List<String> command = task.getCommand("dmg", config);

        String expected = """
            jpackage
            --type
            dmg
            --app-version
            1.0
            --copyright
            Copyright 2025
            --description
            ?
            --icon
            icon.icns
            --name
            Example
            --dest
            mac
            --add-modules
            java.base,java.desktop,java.logging,java.net.http,java.sql,jdk.crypto.ec
            --main-class
            HelloWorld.Main
            --main-jar
            example.jar
            --input
            resources
            --mac-sign
            --mac-app-store
            --mac-entitlements
            entitlements-1234.plist
            --mac-signing-key-user-name
            3rd Party Mac Developer Application: Colorize (F9TKFY3EK3)
            """;

        String cleanCommand = String.join("\n", command)
            .replaceAll("/\\w+/.+/", "")
            .replaceAll("\\d{4}\\d+", "1234");

        assertEquals(expected.trim(), cleanCommand.trim());
    }
}
