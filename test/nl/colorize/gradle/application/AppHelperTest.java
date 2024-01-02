//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.gradle.internal.impldep.org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppHelperTest {

    @Test
    void cleanDirectory(@TempDir File tempDir) throws IOException {
        File a = new File(tempDir, "a");
        a.mkdir();

        File b = new File(a, "b.txt");
        Files.writeString(b.toPath(), "test", UTF_8);

        File c = new File(a, "c");
        c.mkdir();

        File d = new File(c, "d.txt");
        Files.writeString(d.toPath(), "test", UTF_8);

        Project project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build();

        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);

        AppHelper.cleanDirectory(a);

        assertTrue(a.exists());
        assertFalse(b.exists());
        assertFalse(c.exists());
        assertFalse(d.exists());
    }

    @Test
    void getOutputDir(@TempDir File tempDir) {
        Project project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build();

        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);

        File outputDir = AppHelper.getOutputDir(project, "test");

        assertEquals("test", outputDir.getName());
        assertEquals("build", outputDir.getParentFile().getName());
    }
}
