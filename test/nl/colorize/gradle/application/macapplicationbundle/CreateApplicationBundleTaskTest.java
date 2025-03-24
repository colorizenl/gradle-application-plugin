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

import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateApplicationBundleTaskTest {

    @Test
    void createApplicationBundle(@TempDir File tempDir) {
        Project project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build();

        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);

        MacApplicationBundleExt config = new MacApplicationBundleExt();
        config.setName("Example");
        config.setIdentifier("com.example");
        config.setDescription("A description for your application");
        config.setCopyright("Copyright 2025");
        config.setMainJarName("example.jar");
        config.setMainClassName("HelloWorld.Main");
        config.setContentDir("resources");
        config.setBundleVersion("1.0");

        CreateApplicationBundleTask task = (CreateApplicationBundleTask) project.getTasks()
            .getByName("createApplicationBundle");
        task.run(config);

        File bundleDir = new File(tempDir + "/build/mac/Example.app");
        File jdkDir = config.locateEmbeddedJDK(bundleDir);

        assertTrue(bundleDir.exists());
        assertTrue(new File(bundleDir, "Contents").exists());
        assertTrue(new File(bundleDir, "Contents/Java").exists());
        assertTrue(new File(bundleDir, "Contents/Java/example.jar").exists());
        assertTrue(new File(bundleDir, "Contents/MacOS").exists());
        assertTrue(new File(bundleDir, "Contents/MacOS/JavaAppLauncher").exists());
        assertTrue(new File(bundleDir, "Contents/Plugins").exists());
        assertTrue(new File(bundleDir, "Contents/Resources").exists());
        assertTrue(new File(bundleDir, "Contents/Resources/icon.icns").exists());
        assertTrue(new File(bundleDir, "Contents/Info.plist").exists());
        assertTrue(new File(bundleDir, "Contents/PkgInfo").exists());

        assertTrue(jdkDir.exists());
        assertTrue(new File(jdkDir, "Contents/Home").exists());
        assertTrue(new File(jdkDir, "Contents/Home/lib").exists());
        assertTrue(new File(jdkDir, "Contents/Home/lib/jspawnhelper").exists());
        assertTrue(new File(jdkDir, "Contents/Info.plist").exists());
        assertTrue(new File(jdkDir, "Contents/MacOS").exists());
        assertTrue(new File(jdkDir, "Contents/MacOS/libjli.dylib").exists());
    }
}
