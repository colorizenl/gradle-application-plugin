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
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SignApplicationBundleTaskTest {

    private File outputDir;
    private MacApplicationBundleExt config;
    private CreateApplicationBundleTask createTask;
    private SignApplicationBundleTask signTask;

    @BeforeEach
    public void before(@TempDir File inputDir, @TempDir File outputDir) {
        this.outputDir = outputDir;

        Project project = ProjectBuilder.builder().withProjectDir(inputDir).build();
        project.getLayout().getBuildDirectory().set(outputDir);

        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);

        config = project.getExtensions().getByType(MacApplicationBundleExt.class);
        createTask = (CreateApplicationBundleTask) project.getTasks().getByName("createApplicationBundle");
        signTask = (SignApplicationBundleTask) project.getTasks().getByName("signApplicationBundle");
    }

    @Test
    void signApplicationBundle() throws IOException {
        config.getName().set("Example");
        config.getIdentifier().set("com.example");
        config.getMainJarName().set("example.jar");
        config.getMainClassName().set("HelloWorld.Main");
        config.getContentDir().set(new File("resources").getAbsolutePath());
        config.getIcon().set(new File("resources/icon.icns").getAbsolutePath());
        createTask.run(config);
        signTask.run(config);

        File bundle = new File(outputDir, "mac/Example.app");

        assertTrue(bundle.exists());
    }

    @Test
    void extractNativeLibraries() throws IOException {
        config.getName().set("Example");
        config.getIdentifier().set("com.example");
        config.getMainJarName().set("example.jar");
        config.getMainClassName().set("HelloWorld.Main");
        config.getContentDir().set(new File("resources").getAbsolutePath());
        config.getSignNativeLibraries().set(true);
        config.getIcon().set(new File("resources/icon.icns").getAbsolutePath());
        createTask.run(config);
        signTask.run(config);

        File bundle = new File(outputDir, "mac/Example.app");

        assertTrue(bundle.exists());
        assertTrue(new File(bundle, "Contents/MacOS").exists());
        assertTrue(new File(bundle, "Contents/MacOS/native.dylib").exists());
    }
}
