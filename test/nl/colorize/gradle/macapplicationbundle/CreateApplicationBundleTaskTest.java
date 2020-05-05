//-----------------------------------------------------------------------------
// Gradle Mac Application Bundle Plugin
// Copyright 2010-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.macapplicationbundle;

import org.gradle.api.Project;
import org.gradle.internal.impldep.com.google.common.io.Files;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateApplicationBundleTaskTest {

    @Test
    void createApplicationBundle() {
        File tempDir = Files.createTempDir();
        Project project = ProjectBuilder.builder().withProjectDir(tempDir).build();
        ApplicationBundlePlugin plugin = new ApplicationBundlePlugin();
        plugin.apply(project);

        MacApplicationBundleExt config = new MacApplicationBundleExt();
        config.setOutputDir(tempDir.getAbsolutePath());
        config.setName("Example");
        config.setIdentifier("com.example");
        config.setDescription("A description for your application");
        config.setCopyright("Copyright 2020");
        config.setMainClassName("HelloWorld.Main");
        config.setContentDir("resources");
        config.setVersion("1.0");

        CreateApplicationBundleTask task = (CreateApplicationBundleTask) project.getTasks()
            .getByName("createApplicationBundle");
        task.run(config);

        assertTrue(new File(tempDir + "/Example.app").exists());
        assertTrue(new File(tempDir + "/Example.app/Contents").exists());
        assertTrue(new File(tempDir + "/Example.app/Contents/Java").exists());
        assertTrue(new File(tempDir + "/Example.app/Contents/Java/example.jar").exists());
        assertTrue(new File(tempDir + "/Example.app/Contents/MacOS").exists());
        assertTrue(new File(tempDir + "/Example.app/Contents/MacOS/JavaAppLauncher").exists());
        assertTrue(new File(tempDir + "/Example.app/Contents/Plugins").exists());
        assertTrue(new File(tempDir + "/Example.app/Contents/Plugins/adoptopenjdk-11.jdk").exists());
        assertTrue(new File(tempDir + "/Example.app/Contents/Resources").exists());
        assertTrue(new File(tempDir + "/Example.app/Contents/Resources/icon.icns").exists());
        assertTrue(new File(tempDir + "/Example.app/Contents/Info.plist").exists());
        assertTrue(new File(tempDir + "/Example.app/Contents/PkgInfo").exists());
    }
}
