//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Utility class with shared logic between the different tasks and sub-plugins.
 */
public class AppHelper {

    private AppHelper() {
    }

    public static void requireWindows() {
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            throw new UnsupportedOperationException("This task can only be used on Windows");
        }
    }

    public static void requireMac() {
        if (!System.getProperty("os.name").toLowerCase().contains("mac")) {
            throw new UnsupportedOperationException("This task can only be used on Mac");
        }
    }

    public static File getLibsDir(Project project) {
        File libsDir = (File) project.getProperties().get("libsDir");
        if (libsDir != null) {
            return libsDir;
        }

        // Gradle 7 and higher no longer have the libsDir property.
        File buildDir = project.getBuildDir();
        String libsDirName = (String) project.getProperties().get("libsDirName");
        return new File(buildDir, libsDirName);
    }

    public static String getJarFileName(Project project) {
        return (String) project.getProperties().get("jar.archiveFileName");
    }

    public static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    public static String getEnvironmentVariable(String name) {
        String value = System.getenv(name);
        check(value != null && !value.isEmpty(), "Missing environment variable: " + name);
        return value;
    }

    public static File getProjectDir(Project project, String name) {
        if (name.startsWith("/")) {
            return new File(name);
        } else {
            return new File(project.getProjectDir().getAbsolutePath() + "/" + name);
        }
    }

    public static File getProjectFile(Project project, String name) {
        return getProjectDir(project, name);
    }

    public static File getOutputDir(Project project, String name) {
        File outputDir = new File(project.getBuildDir().getAbsolutePath() + "/" + name);
        if (!project.getBuildDir().exists()) {
            project.getBuildDir().mkdir();
        }
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        return outputDir;
    }

    /**
     * Makes sure the application bundle directory is empty before we start.
     * This is necessary because Gradle uses very aggressive caching, and JLink
     * will not work unless the directory is empty.
     */
    public static void cleanDirectory(File dir) {
        if (dir.exists()) {
            try {
                Files.walk(dir.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            } catch (IOException e) {
                throw new RuntimeException("Unable to delete: " + dir.getAbsolutePath());
            }
        }

        dir.mkdir();
    }

    public static File mkdir(File dir) {
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                throw new IllegalStateException("Unable to create " + dir.getAbsolutePath());
            }
        }
        return dir;
    }

    public static String loadResourceFile(String path) {
        try (InputStream stream = AppHelper.class.getClassLoader().getResourceAsStream(path)) {
            check(stream != null, "Unable to locate resource file: " + path);
            byte[] contents = stream.readAllBytes();
            return new String(contents, UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Resource file not found: " + path);
        }
    }

    /**
     * Loads the specified resource file into a string, and then substitutes
     * the specified placeholders with the provided values.
     */
    public static String loadResourceFile(String path, Map<String, String> properties) {
        String contents = loadResourceFile(path);
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            contents = contents.replace(entry.getKey(), entry.getValue());
        }
        return contents;
    }

    public static void clearOutputDir(File outputDir) {
        if (!outputDir.exists()) {
            return;
        }

        try {
            Files.walk(outputDir.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .filter(file -> !file.equals(outputDir))
                .forEach(File::delete);
        } catch (IOException e) {
            throw new RuntimeException("Unable to clear directory: " + outputDir.getAbsolutePath());
        }
    }
}
