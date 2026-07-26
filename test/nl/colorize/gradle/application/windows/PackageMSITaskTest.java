//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.windows;

import nl.colorize.gradle.application.ApplicationPlugin;
import nl.colorize.gradle.application.macapplicationbundle.MacApplicationBundleExt;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PackageMSITaskTest {

    private File outputDir;
    private WindowsInstallerExt config;
    private MacApplicationBundleExt macConfig;
    private PackageMSITask task;

    @BeforeEach
    public void before(@TempDir File inputDir, @TempDir File outputDir) {
        this.outputDir = outputDir;

        Project project = ProjectBuilder.builder().withProjectDir(inputDir).build();
        project.getLayout().getBuildDirectory().set(outputDir);

        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);

        config = project.getExtensions().getByType(WindowsInstallerExt.class);
        macConfig = project.getExtensions().getByType(MacApplicationBundleExt.class);
        task = (PackageMSITask) project.getTasks().getByName("packageMSI");
    }

    @Test
    void inheritConfiguration() {
        macConfig.getName().set("Example");
        macConfig.getIdentifier().set("com.example");
        macConfig.getBundleVersion().set("1.0");
        macConfig.getDescription().set("A simple example application");
        macConfig.getCopyright().set("Copyright 2010-2026 Colorize");
        macConfig.getIcon().set("resources/icon.icns");
        macConfig.getMainJarName().set("example.jar");
        macConfig.getMainClassName().set("com.example.ExampleApp");

        config.getIcon().set(new File("resources/icon.ico").getAbsolutePath());
        config.getUuid().set("b9112b5f-2340-4541-8883-6abd3c9c8780");
        config.getMainJarName().set("example.jar");

        List<String> command = task.buildPackageCommand(config).stream()
            .map(e -> e == null ? "<null>" : e)
            .map(e -> e.replace(outputDir.getAbsolutePath(), "/tmp").replace("/private", ""))
            .map(e -> e.replace(new File("resources").getAbsolutePath(), "{currentdir}"))
            .toList();

        String expected = """
            jpackage
            --type
            msi
            --input
            /tmp/libs
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
            Copyright 2010-2026 Colorize
            --icon
            {currentdir}/icon.ico
            --win-upgrade-uuid
            b9112b5f-2340-4541-8883-6abd3c9c8780
            --win-per-user-install
            --win-menu
            --win-shortcut
            --dest
            /tmp/windows-msi""";

        assertEquals(expected, String.join("\n", command));
    }
}
