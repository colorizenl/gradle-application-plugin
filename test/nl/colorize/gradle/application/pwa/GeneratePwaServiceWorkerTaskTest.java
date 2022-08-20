//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.pwa;

import nl.colorize.gradle.application.ApplicationPlugin;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratePwaServiceWorkerTaskTest {

    @Test
    void generateServiceWorker(@TempDir File tempDir) throws IOException {
        Project project = ProjectBuilder.builder().withProjectDir(tempDir).build();
        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);

        File inputDir = new File(tempDir, "input");
        inputDir.mkdir();
        Files.writeString(new File(inputDir, "first.js").toPath(), "console.log(1);", UTF_8);
        File subDir = new File(inputDir, "sub");
        subDir.mkdir();
        Files.writeString(new File(subDir, "second.js").toPath(), "console.log(2);", UTF_8);

        PwaExt config = new PwaExt();
        config.setWebAppDir(inputDir);
        config.setServiceWorkerFile(new File(tempDir, "out.js"));
        config.setPwaName("Test");
        config.setPwaVersion("0.2");

        GeneratePwaServiceWorkerTask task = (GeneratePwaServiceWorkerTask) project.getTasks()
            .getByName("generatePwaServiceWorker");
        task.run(config);

        String expected = "";
        expected += "const CACHE_KEY = \"Test-0.2\";\n";
        expected += "\n";
        expected += "const RESOURCE_FILES = [\n";
        expected += "    \"/first.js\",\n";
        expected += "    \"/sub/second.js\",\n";
        expected += "];\n";
        expected += "\n";
        expected += "self.addEventListener(\"install\", e => {\n";
        expected += "    e.waitUntil((async () => {\n";
        expected += "        const cache = await caches.open(CACHE_KEY);\n";
        expected += "        await cache.addAll(RESOURCE_FILES);\n";
        expected += "    })());\n";
        expected += "});\n";

        assertTrue(config.getServiceWorkerFile().exists());
        assertEquals(expected, Files.readString(config.getServiceWorkerFile().toPath(), UTF_8));
    }
}
