//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

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
}
