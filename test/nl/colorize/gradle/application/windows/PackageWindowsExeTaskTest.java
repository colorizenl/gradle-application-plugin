//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.windows;

import nl.colorize.gradle.application.AppHelper;
import nl.colorize.gradle.application.ApplicationPlugin;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackageWindowsExeTaskTest {

    private File outputDir;
    private WindowsStandaloneExt config;
    private PackageWindowsExeTask task;

    @BeforeEach
    public void before(@TempDir File inputDir, @TempDir File outputDir) {
        this.outputDir = outputDir;

        Project project = ProjectBuilder.builder().withProjectDir(inputDir).build();
        project.getLayout().getBuildDirectory().set(outputDir);

        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);

        config = project.getExtensions().getByType(WindowsStandaloneExt.class);
        task = (PackageWindowsExeTask) project.getTasks().getByName("packageEXE");
    }

    @Test
    void generateLaunch4jConfig() throws IOException {
        File libsDir = AppHelper.mkdir(new File(outputDir, "libs"));
        Files.copy(new File("example/resources/example.jar").toPath(),
            new File(libsDir, "example.jar").toPath());

        config.getMainJarName().set("example.jar");
        config.getName().set("Example");
        config.getVersion().set("1.0");
        config.getIcon().set(new File("resources/icon.ico").getAbsolutePath());
        config.getSupportURL().set("https://www.colorize.nl");

        File result = task.generateLaunch4jConfig(config);

        String contents = Files.readString(result.toPath(), UTF_8)
            .replace("/private", "")
            .replace(outputDir.getAbsolutePath(), "{tempdir}")
            .replace(new File("resources").getAbsolutePath(), "{currentdir}");

        String expected = """
            <?xml version="1.0" encoding="UTF-8" ?>
            <launch4jConfig>
                <dontWrapJar>true</dontWrapJar>
                <headerType>gui</headerType>
                <jar>example.jar</jar>
                <outfile>{tempdir}/example.exe</outfile>
                <errTitle>Error</errTitle>
                <cmdLine></cmdLine>
                <chdir>.</chdir>
                <priority>normal</priority>
                <downloadUrl>https://www.colorize.nl</downloadUrl>
                <supportUrl>https://www.colorize.nl</supportUrl>
                <stayAlive>false</stayAlive>
                <restartOnCrash>false</restartOnCrash>
                <manifest></manifest>
                <icon>{currentdir}/icon.ico</icon>
                <jre>
                    <path>java</path>
                    <bundledJre64Bit>true</bundledJre64Bit>
                    <bundledJreAsFallback>false</bundledJreAsFallback>
                    <minVersion>25</minVersion>
                    <maxVersion></maxVersion>
                    <jdkPreference>preferJre</jdkPreference>
                    <runtimeBits>64/32</runtimeBits>
                    <maxHeapSize>2048</maxHeapSize>
                </jre>
            </launch4jConfig>
            """;

        assertEquals(expected, contents);
    }

    @Test
    void generateWindowsApplication() throws IOException {
        File libsDir = AppHelper.mkdir(new File(outputDir, "libs"));
        Files.copy(new File("example/resources/example.jar").toPath(),
            new File(libsDir, "example.jar").toPath());

        config.getMainJarName().set("example.jar");
        config.getName().set("Example");
        config.getVersion().set("1.0");
        config.getIcon().set(new File("resources/icon.ico").getAbsolutePath());
        config.getSupportURL().set("https://www.colorize.nl");
        task.run(config);

        assertTrue(new File(outputDir, "example-windows.zip").exists());
        assertFalse(new File(outputDir, "example.exe").exists());
        assertFalse(new File(outputDir, "launch4j.xml").exists());
    }
}
