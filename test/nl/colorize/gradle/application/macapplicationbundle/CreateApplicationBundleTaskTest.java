//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.macapplicationbundle;

import nl.colorize.gradle.application.ApplicationPlugin;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateApplicationBundleTaskTest {

    private File outputDir;
    private MacApplicationBundleExt config;
    private CreateApplicationBundleTask task;

    @BeforeEach
    public void before(@TempDir File inputDir, @TempDir File outputDir) {
        this.outputDir = outputDir;

        Project project = ProjectBuilder.builder().withProjectDir(inputDir).build();
        project.getLayout().getBuildDirectory().set(outputDir);

        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);

        config = project.getExtensions().getByType(MacApplicationBundleExt.class);
        task = (CreateApplicationBundleTask) project.getTasks().getByName("createApplicationBundle");
    }

    @Test
    void createApplicationBundle() {
        config.getName().set("Example");
        config.getIdentifier().set("com.example");
        config.getDescription().set("A description for your application");
        config.getCopyright().set("Copyright 2026");
        config.getMainJarName().set("example.jar");
        config.getMainClassName().set("HelloWorld.Main");
        config.getContentDir().set(new File("resources").getAbsolutePath());
        config.getBundleVersion().set("1.0");
        config.getAdditionalBinaries().set(List.of("resources/App.swift"));
        config.getIcon().set(new File("resources/icon.icns").getAbsolutePath());
        task.run(config);

        File bundleDir = new File(outputDir, "mac/Example.app");
        File jdkDir = config.locateEmbeddedJDK(bundleDir);

        assertTrue(bundleDir.exists());
        assertTrue(new File(bundleDir, "Contents").exists());
        assertTrue(new File(bundleDir, "Contents/Java").exists());
        assertTrue(new File(bundleDir, "Contents/Java/example.jar").exists());
        assertTrue(new File(bundleDir, "Contents/MacOS").exists());
        assertTrue(new File(bundleDir, "Contents/MacOS/JavaAppLauncher").exists());
        assertTrue(new File(bundleDir, "Contents/MacOS/App.swift").exists());
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
