//-----------------------------------------------------------------------------
// Gradle Mac Application Bundle Plugin
// Copyright 2010-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.macapplicationbundle;

import org.gradle.internal.impldep.com.google.common.io.Files;
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationBundlePluginTest {

    @Test
    void usePlugin() throws IOException {
        String gradle = "";
        gradle += "plugins {\n";
        gradle += "    id 'nl.colorize.gradle.macapplicationbundle' version '2020.2'\n";
        gradle += "}\n";
        gradle += "\n";
        gradle += "macApplicationBundle {\n";
        gradle += "    name = 'Example'\n";
        gradle += "    identifier = 'com.example'\n";
        gradle += "    description = 'A description for your application'\n";
        gradle += "    copyright = 'Copyright 2020'\n";
        gradle += "    mainClassName = 'HelloWorld.Main'\n";
        gradle += "    contentDir = 'resources'\n";
        gradle += "    version = '1.0'\n";
        gradle += "}\n";

        TemporaryFolder tempDir = new TemporaryFolder();
        tempDir.create();

        File buildFile = tempDir.newFile("build.gradle");
        Files.write(gradle, buildFile, StandardCharsets.UTF_8);

        File resourceDir = tempDir.newFolder("resources");
        Files.copy(new File("resources/example.jar"), new File(resourceDir, "example.jar"));

        BuildResult result = GradleRunner.create()
            .withProjectDir(tempDir.getRoot())
            .withArguments("createApplicationBundle")
            .build();

        assertEquals("", result.getOutput());
    }
}
