//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.macapplicationbundle;

import nl.colorize.gradle.application.AppHelper;
import nl.colorize.gradle.application.ApplicationPlugin;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PackageApplicationBundleTaskTest {

    private File inputDir;
    private MacApplicationBundleExt config;
    private PackageApplicationBundleTask task;

    @BeforeEach
    public void before(@TempDir File inputDir, @TempDir File outputDir) {
        this.inputDir = inputDir;

        Project project = ProjectBuilder.builder().withProjectDir(inputDir).build();
        project.getLayout().getBuildDirectory().set(outputDir);

        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);

        config = project.getExtensions().getByType(MacApplicationBundleExt.class);
        task = (PackageApplicationBundleTask) project.getTasks().getByName("packageApplicationBundle");
    }

    @Test
    void runJPackage() {
        AppHelper.copyDirectory(new File("resources"), new File(inputDir, "resources"));

        config.getName().set("Example");
        config.getIdentifier().set("com.example");
        config.getMainJarName().set("example.jar");
        config.getMainClassName().set("HelloWorld.Main");
        config.getContentDir().set("resources");
        config.getDescription().set("?");

        List<String> command = task.getCommand("dmg", config);

        String expected = """
            jpackage
            --type
            dmg
            --app-version
            1.0
            --copyright
            Copyright 2026
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
