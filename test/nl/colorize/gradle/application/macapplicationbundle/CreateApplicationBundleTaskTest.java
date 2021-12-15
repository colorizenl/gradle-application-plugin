//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.macapplicationbundle;

import nl.colorize.gradle.application.ApplicationPlugin;
import org.gradle.api.Project;
import org.gradle.internal.impldep.com.google.common.io.Files;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateApplicationBundleTaskTest {

    @Test
    void createApplicationBundle() {
        File tempDir = Files.createTempDir();
        Project project = ProjectBuilder.builder().withProjectDir(tempDir).build();
        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);

        MacApplicationBundleExt config = new MacApplicationBundleExt();
        config.setOutputDir(tempDir.getAbsolutePath());
        config.setName("Example");
        config.setIdentifier("com.example");
        config.setDescription("A description for your application");
        config.setCopyright("Copyright 2021");
        config.setMainClassName("HelloWorld.Main");
        config.setContentDir("resources");
        config.setBundleVersion("1.0");

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
        assertTrue(new File(tempDir + "/Example.app/Contents/Plugins/temurin-17.jdk").exists());
        assertTrue(new File(tempDir + "/Example.app/Contents/Resources").exists());
        assertTrue(new File(tempDir + "/Example.app/Contents/Resources/icon.icns").exists());
        assertTrue(new File(tempDir + "/Example.app/Contents/Info.plist").exists());
        assertTrue(new File(tempDir + "/Example.app/Contents/PkgInfo").exists());
    }

    @Test
    void includePlugins() throws IOException {
        File tempDir = Files.createTempDir();
        Project project = ProjectBuilder.builder().withProjectDir(tempDir).build();
        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);

        File tempPluginDir = Files.createTempDir();
        File pluginDir = new File(tempPluginDir, "MyPlugin");
        pluginDir.mkdir();
        Files.write("test", new File(pluginDir, "file.txt"), StandardCharsets.UTF_8);

        MacApplicationBundleExt config = new MacApplicationBundleExt();
        config.setOutputDir(tempDir.getAbsolutePath());
        config.setContentDir(Files.createTempDir().getAbsolutePath());
        config.setName("Example");
        config.setIdentifier("com.example");
        config.setMainClassName("HelloWorld.Main");
        config.getPluginDirs().add(pluginDir.getAbsolutePath());

        CreateApplicationBundleTask task = (CreateApplicationBundleTask) project.getTasks()
            .getByName("createApplicationBundle");
        task.run(config);

        assertTrue(new File(tempDir + "/Example.app").exists());
        assertTrue(new File(tempDir + "/Example.app/Contents").exists());
        assertTrue(new File(tempDir + "/Example.app/Contents/Plugins").exists());
        assertTrue(new File(tempDir + "/Example.app/Contents/Plugins/MyPlugin").exists());
        assertTrue(new File(tempDir + "/Example.app/Contents/Plugins/MyPlugin/file.txt").exists());
    }
}
