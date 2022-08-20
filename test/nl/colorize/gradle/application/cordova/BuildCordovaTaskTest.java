//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.cordova;

import nl.colorize.gradle.application.ApplicationPlugin;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This is more of an integration test than a unit test, since it actually
 * creates Cordova apps. This also means that Cordova and the target platforms
 * need to be available in the environment running the tests.
 */
class BuildCordovaTaskTest {

    @Test
    void createDebugVersionApps(@TempDir File tempDir) throws IOException {
        Project project = ProjectBuilder.builder().withProjectDir(tempDir).build();
        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);

        String json = "";
        json += "{\n";
        json += "    \"ios\": {\n";
        json += "        \"debug\": {\n";
        json += "            \"codeSignIdentity\": \"Colorize (F9TKFY3EK3)\",\n";
        json += "            \"packageType\": \"development\",\n";
        json += "            \"automaticProvisioning\": true,\n";
        json += "            \"buildFlag\": []\n";
        json += "        },\n";
        json += "        \"release\": {\n";
        json += "        }\n";
        json += "    },\n";
        json += "    \"android\": {\n";
        json += "        \"debug\": {\n";
        json += "        },\n";
        json += "        \"release\": {\n";
        json += "        }\n";
        json += "    },\n";
        json += "    \"osx\": {\n";
        json += "        \"debug\": {\n";
        json += "            \"codeSignIdentity\": \"3rd Party Mac Developer Application: Colorize (F9TKFY3EK3)\",\n";
        json += "            \"packageType\": \"development\",\n";
        json += "            \"automaticProvisioning\": false\n";
        json += "        },\n";
        json += "        \"release\": {\n";
        json += "        }\n";
        json += "    }\n";
        json += "}\n";

        File buildJsonFile = File.createTempFile("build", ".json");
        Files.writeString(buildJsonFile.toPath(), json);

        CordovaExt config = new CordovaExt();
        config.setWebAppDir(new File("resources").getAbsolutePath());
        config.setAppId("nl.colorize.test");
        config.setAppName("Example");
        config.setDisplayVersion("0.1.2");
        config.setIcon(new File("resources/icon.png").getAbsolutePath());
        config.setBuildJson(buildJsonFile.getAbsolutePath());
        config.setDist("debug");
        config.setPlatforms("android");

        BuildCordovaTask task = (BuildCordovaTask) project.getTasks().getByName("buildCordova");
        task.run(config);

        assertTrue(tempDir.exists());
    }
}
