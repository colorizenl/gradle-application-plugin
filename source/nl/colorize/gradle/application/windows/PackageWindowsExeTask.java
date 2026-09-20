//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.windows;

import nl.colorize.gradle.application.AppHelper;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecSpec;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class PackageWindowsExeTask extends DefaultTask {

    @Nested
    public abstract Property<WindowsStandaloneExt> getExt();

    @Inject
    public abstract ExecOperations getExecService();

    @TaskAction
    public void run() {
        run(getExt().get());
    }

    protected void run(WindowsStandaloneExt config) {
        File xmlFile = generateLaunch4jConfig(config);
        getExecService().exec(exec -> runLaunch4j(exec, xmlFile));
        AppHelper.delete(xmlFile);
        packageWindowsApplication(config);

        // Clean up the generated EXE file since it's already
        // been packaged.
        File exeFile = config.locateExeFile();
        AppHelper.delete(exeFile);
    }

    protected File generateLaunch4jConfig(WindowsStandaloneExt config) {
        File xmlFile = config.toOutputFile("launch4j.xml");
        File jarFile = getMainJarFile(config);
        File exeFile = config.locateExeFile();

        try (PrintWriter writer = new PrintWriter(xmlFile, UTF_8)) {
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
            writer.println("<launch4jConfig>");
            writer.println("    <dontWrapJar>true</dontWrapJar>");
            writer.println("    <headerType>gui</headerType>");
            writer.println("    <jar>" + jarFile.getName() + "</jar>");
            writer.println("    <outfile>" + exeFile.getAbsolutePath() + "</outfile>");
            writer.println("    <errTitle>Error</errTitle>");
            writer.println("    <cmdLine></cmdLine>");
            writer.println("    <chdir>.</chdir>");
            writer.println("    <priority>normal</priority>");
            writer.println("    <downloadUrl>" + config.getSupportURL().get() + "</downloadUrl>");
            writer.println("    <supportUrl>" + config.getSupportURL().get() + "</supportUrl>");
            writer.println("    <stayAlive>false</stayAlive>");
            writer.println("    <restartOnCrash>false</restartOnCrash>");
            writer.println("    <manifest></manifest>");
            writer.println("    <icon>" + getIconFile(config).getAbsolutePath() + "</icon>");
            writer.println("    <jre>");
            writer.println("        <path>java</path>");
            writer.println("        <bundledJre64Bit>true</bundledJre64Bit>");
            writer.println("        <bundledJreAsFallback>false</bundledJreAsFallback>");
            writer.println("        <minVersion>" + config.getJavaVersion().get() + "</minVersion>");
            writer.println("        <maxVersion></maxVersion>");
            writer.println("        <jdkPreference>preferJre</jdkPreference>");
            writer.println("        <runtimeBits>64/32</runtimeBits>");
            writer.println("        <maxHeapSize>" + config.getMemory().get() + "</maxHeapSize>");
            writer.println("    </jre>");
            writer.println("</launch4jConfig>");
        } catch (IOException e) {
            throw new RuntimeException("Error while generating " + xmlFile.getAbsolutePath(), e);
        }

        return xmlFile;
    }

    private void runLaunch4j(ExecSpec exec, File xmlFile) {
        String launch4j = AppHelper.getEnvironmentVariable("LAUNCH4J_HOME") + "/launch4j.jar";
        exec.commandLine("java", "-Djava.awt.headless=true", "-jar", launch4j, xmlFile.getAbsolutePath());
    }

    private void packageWindowsApplication(WindowsStandaloneExt config) {
        File runtime = new File(AppHelper.getEnvironmentVariable("EMBEDDED_WINDOWS_JAVA"));
        File jarFile = getMainJarFile(config);
        File exeFile = config.locateExeFile();
        File zipFile = config.toOutputFile(exeFile.getName().replace(".exe", "-windows.zip"));

        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile), UTF_8)) {
            addZipEntry(zip, jarFile.getName(), jarFile.toPath());
            addZipEntry(zip, exeFile.getName(), exeFile.toPath());

            try (Stream<Path> stream = Files.walk(runtime.toPath())) {
                stream.filter(path -> !Files.isDirectory(path))
                    .filter(path -> !shouldSkipFile(path))
                    .forEach(path -> addZipEntry(zip, runtime, path));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error creating ZIP file", e);
        }
    }

    private boolean shouldSkipFile(Path file) {
        Path fileName = file.getFileName();
        return fileName == null || fileName.toString().equals(".DS_Store");
    }

    private void addZipEntry(ZipOutputStream zip, File runtime, Path file) {
        String zipPath = runtime.toPath().relativize(file).toString();
        addZipEntry(zip, zipPath, file);
    }

    private void addZipEntry(ZipOutputStream zip, String zipPath, Path file) {
        try {
            zip.putNextEntry(new ZipEntry(zipPath));
            Files.copy(file, zip);
            zip.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException("Error writing ZIP file entry", e);
        }
    }

    private File getMainJarFile(WindowsStandaloneExt config) {
        File jarFile = new File(config.locateLibsDir(), config.getMainJarName().get());
        if (!jarFile.exists()) {
            throw new IllegalStateException("Cannot locate JAR file: " + jarFile.getAbsolutePath());
        }
        return jarFile;
    }

    private File getIconFile(WindowsStandaloneExt config) {
        File iconFile = config.toProjectFile(config.getIcon().get());
        if (!iconFile.exists()) {
            throw new IllegalStateException("Cannot locate icon file: " + iconFile.getAbsolutePath());
        }
        return iconFile;
    }
}
