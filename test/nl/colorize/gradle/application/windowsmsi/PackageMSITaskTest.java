//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.windowsmsi;

import nl.colorize.gradle.application.ApplicationPlugin;
import nl.colorize.gradle.application.macapplicationbundle.MacApplicationBundleExt;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PackageMSITaskTest {

    @Test
    void inheritConfiguration(@TempDir File tempDir) {
        MacApplicationBundleExt macConfig = new MacApplicationBundleExt();
        macConfig.setName("Example");
        macConfig.setIdentifier("com.example");
        macConfig.setBundleVersion("1.0");
        macConfig.setDescription("A simple example application");
        macConfig.setCopyright("Copyright 2010-2024 Colorize");
        macConfig.setIcon("resources/icon.icns");
        macConfig.setMainJarName("example.jar");
        macConfig.setMainClassName("com.example.ExampleApp");

        WindowsInstallerExt windowsConfig = new WindowsInstallerExt();
        windowsConfig.setInherit(true);
        windowsConfig.setIcon("resources/icon.ico");
        windowsConfig.setUuid("b9112b5f-2340-4541-8883-6abd3c9c8780");
        windowsConfig.setMainJarName("example.jar");

        windowsConfig.inherit(macConfig);

        Project project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build();

        project.getPluginManager().apply(JavaPlugin.class);

        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);

        PackageMSITask msiTask = (PackageMSITask) project.getTasks().getByName("packageMSI");
        List<String> command = msiTask.buildPackageCommand(windowsConfig).stream()
            .map(e -> e == null ? "<null>" : e)
            .map(e -> e.replace(tempDir.getAbsolutePath(), "/tmp").replace("/private", ""))
            .toList();

        String expected = """
            jpackage
            --type
            msi
            --input
            /tmp/build/libs
            --main-jar
            example.jar
            --main-class
            com.example.ExampleApp
            --name
            Example
            --app-version
            1.0
            --description
            A simple example application
            --copyright
            Copyright 2010-2024 Colorize
            --icon
            resources/icon.ico
            --win-upgrade-uuid
            b9112b5f-2340-4541-8883-6abd3c9c8780
            --win-per-user-install
            --win-menu
            --win-shortcut
            --dest
            /tmp/build/windows-msi""";

        assertEquals(expected, String.join("\n", command));
    }
}
