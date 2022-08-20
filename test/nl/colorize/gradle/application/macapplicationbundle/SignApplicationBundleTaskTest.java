//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.macapplicationbundle;

import nl.colorize.gradle.application.ApplicationPlugin;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SignApplicationBundleTaskTest {

    @Test
    void signApplicationBundle(@TempDir File tempDir) throws IOException {
        Project project = ProjectBuilder.builder().withProjectDir(tempDir).build();
        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);

        MacApplicationBundleExt config = new MacApplicationBundleExt();
        config.setOutputDir(tempDir.getAbsolutePath());
        config.setName("Example");
        config.setIdentifier("com.example");
        config.setMainClassName("HelloWorld.Main");
        config.setContentDir("resources");
        config.setSignIdentityApp("3rd Party Mac Developer Application: Colorize (F9TKFY3EK3)");
        config.setSignIdentityInstaller("3rd Party Mac Developer Installer: Colorize (F9TKFY3EK3)");

        CreateApplicationBundleTask createTask = (CreateApplicationBundleTask) project.getTasks()
            .getByName("createApplicationBundle");
        createTask.run(config);

        SignApplicationBundleTask signTask = (SignApplicationBundleTask) project.getTasks()
            .getByName("signApplicationBundle");
        signTask.run(config);

        File bundle = new File(tempDir + "/Example.app");

        assertTrue(bundle.exists());
    }
}
