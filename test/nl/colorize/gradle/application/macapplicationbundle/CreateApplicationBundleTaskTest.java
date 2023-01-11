//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.macapplicationbundle;

import nl.colorize.gradle.application.ApplicationPlugin;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateApplicationBundleTaskTest {

    @Test
    void createApplicationBundleJLink(@TempDir File tempDir) {
        Project project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build();

        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);

        MacApplicationBundleExt config = new MacApplicationBundleExt();
        config.setName("Example");
        config.setIdentifier("com.example");
        config.setDescription("A description for your application");
        config.setCopyright("Copyright 2023");
        config.setMainClassName("HelloWorld.Main");
        config.setContentDir("resources");
        config.setBundleVersion("1.0");

        CreateApplicationBundleTask task = (CreateApplicationBundleTask) project.getTasks()
            .getByName("createApplicationBundle");
        task.run(config);

        assertTrue(new File(tempDir + "/build/mac/Example.app").exists());
        assertTrue(new File(tempDir + "/build/mac/Example.app/Contents").exists());
        assertTrue(new File(tempDir + "/build/mac/Example.app/Contents/Java").exists());
        assertTrue(new File(tempDir + "/build/mac/Example.app/Contents/Java/example.jar").exists());
        assertTrue(new File(tempDir + "/build/mac/Example.app/Contents/MacOS").exists());
        assertTrue(new File(tempDir + "/build/mac/Example.app/Contents/MacOS/JavaAppLauncher").exists());
        assertTrue(new File(tempDir + "/build/mac/Example.app/Contents/Plugins").exists());
        assertTrue(new File(tempDir + "/build/mac/Example.app/Contents/Plugins/temurin-17.jdk").exists());
        assertTrue(new File(tempDir + "/build/mac/Example.app/Contents/Resources").exists());
        assertTrue(new File(tempDir + "/build/mac/Example.app/Contents/Resources/icon.icns").exists());
        assertTrue(new File(tempDir + "/build/mac/Example.app/Contents/Info.plist").exists());
        assertTrue(new File(tempDir + "/build/mac/Example.app/Contents/PkgInfo").exists());
    }
}
