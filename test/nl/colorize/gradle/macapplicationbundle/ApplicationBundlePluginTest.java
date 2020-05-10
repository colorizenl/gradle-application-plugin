//-----------------------------------------------------------------------------
// Gradle Mac Application Bundle Plugin
// Copyright 2010-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.macapplicationbundle;

import org.gradle.internal.impldep.com.google.common.io.Files;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationBundlePluginTest {

    @Test
    void usePlugin() throws IOException {
        String gradle = "";
        gradle += "plugins {\n";
        gradle += "    id 'nl.colorize.gradle.macapplicationbundle' version '2020.5'\n";
        gradle += "}\n";
        gradle += "\n";
        gradle += "apply plugin: 'base'\n";
        gradle += "\n";
        gradle += "macApplicationBundle {\n";
        gradle += "    name = 'Example'\n";
        gradle += "    identifier = 'com.example'\n";
        gradle += "    description = 'A description for your application'\n";
        gradle += "    copyright = 'Copyright 2020'\n";
        gradle += "    mainClassName = 'HelloWorld.Main'\n";
        gradle += "    contentDir = 'resources'\n";
        gradle += "    version = '1.0'\n";
        gradle += "    outputDir = 'build'\n";
        gradle += "}\n";

        File tempDir = Files.createTempDir();
        tempDir.mkdir();

        File buildFile = new File(tempDir, "build.gradle");
        Files.write(gradle, buildFile, StandardCharsets.UTF_8);

        File resourceDir = new File(tempDir, "resources");
        resourceDir.mkdir();
        Files.copy(new File("resources/example.jar"), new File(resourceDir, "example.jar"));

        BuildResult result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments("clean", "createApplicationBundle")
            .build();

        assertTrue(result.getOutput().contains("> Task :createApplicationBundle"));
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
    }
}
