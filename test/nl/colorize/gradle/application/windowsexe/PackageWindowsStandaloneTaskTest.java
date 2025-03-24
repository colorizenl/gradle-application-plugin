//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.windowsexe;

import nl.colorize.gradle.application.AppHelper;
import nl.colorize.gradle.application.ApplicationPlugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackageWindowsStandaloneTaskTest {

    @Test
    void generateLaunch4jConfig(@TempDir File tempDir) throws IOException {
        File buildDir = AppHelper.mkdir(new File(tempDir, "build"));
        File libsDir = AppHelper.mkdir(new File(buildDir, "libs"));
        Files.copy(new File("example/resources/example.jar").toPath(),
            new File(libsDir, "example.jar").toPath());

        WindowsStandaloneExt config = new WindowsStandaloneExt();
        config.setMainJarName("example.jar");
        config.setName("Example");
        config.setVersion("1.0");
        config.setIcon("resources/icon.ico");
        config.setSupportURL("https://www.colorize.nl");

        PackageWindowsStandaloneTask windowsTask = prepareTask(tempDir);
        File result = windowsTask.generateLaunch4jConfig(config);

        String contents = Files.readString(result.toPath(), UTF_8)
            .replace("/private", "")
            .replace(tempDir.getAbsolutePath(), "{tempdir}")
            .replace(new File("resources").getAbsolutePath(), "{currentdir}");

        String expected = """
            <?xml version="1.0" encoding="UTF-8" ?>
            <launch4jConfig>
                <dontWrapJar>true</dontWrapJar>
                <headerType>gui</headerType>
                <jar>example.jar</jar>
                <outfile>{tempdir}/build/example.exe</outfile>
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
                    <minVersion>17</minVersion>
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
    void generateWindowsApplication(@TempDir File tempDir) throws IOException {
        File buildDir = AppHelper.mkdir(new File(tempDir, "build"));
        File libsDir = AppHelper.mkdir(new File(buildDir, "libs"));
        Files.copy(new File("example/resources/example.jar").toPath(),
            new File(libsDir, "example.jar").toPath());

        WindowsStandaloneExt config = new WindowsStandaloneExt();
        config.setMainJarName("example.jar");
        config.setName("Example");
        config.setVersion("1.0");
        config.setIcon("resources/icon.ico");
        config.setSupportURL("https://www.colorize.nl");

        PackageWindowsStandaloneTask windowsTask = prepareTask(tempDir);
        windowsTask.run(config);

        assertTrue(new File(buildDir, "example-windows.zip").exists());
        assertFalse(new File(buildDir, "example.exe").exists());
        assertFalse(new File(buildDir, "launch4j.xml").exists());
    }

    private PackageWindowsStandaloneTask prepareTask(File tempDir) {
        Project project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build();

        project.getPluginManager().apply(JavaPlugin.class);

        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.apply(project);

        return (PackageWindowsStandaloneTask) project.getTasks().getByName("packageEXE");
    }
}
