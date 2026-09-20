//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

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

    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    public static void requireMac() {
        if (!isMac()) {
            throw new UnsupportedOperationException("This task can only be used on Mac");
        }
    }

    public static String getEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException("Missing environment variable: " + name);
        }
        return value;
    }

    /**
     * Makes sure the application bundle directory is empty before we start.
     * This is necessary because Gradle uses very aggressive caching, and JLink
     * will not work unless the directory is empty.
     */
    public static void cleanDirectory(File dir) {
        if (dir.exists()) {
            try (Stream<Path> stream = Files.walk(dir.toPath())) {
                stream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .filter(file -> !file.equals(dir))
                    .forEach(File::delete);
            } catch (IOException e) {
                throw new RuntimeException("Unable to delete: " + dir.getAbsolutePath());
            }
        }
    }

    public static List<File> walk(File start, Predicate<File> filter) {
        try (Stream<Path> stream = Files.walk(start.toPath())) {
            return stream.map(Path::toFile)
                .filter(filter)
                .toList();
        } catch (IOException e) {
            throw new RuntimeException("Error while walking " + start.getAbsolutePath(), e);
        }
    }

    public static File mkdir(File dir) {
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                throw new IllegalStateException("Unable to create " + dir.getAbsolutePath());
            }
        }
        return dir;
    }

    public static void delete(File file) {
        if (file.exists() && !file.delete()) {
            throw new IllegalStateException("Failed to delete file " + file.getAbsolutePath());
        }
    }

    public static String loadResourceFile(String path) {
        try (InputStream stream = AppHelper.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Resource not found: " + path);
            }
            byte[] contents = stream.readAllBytes();
            return new String(contents, UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Resource file not found: " + path);
        }
    }

    /**
     * Loads a template from the specified classpath resource, then rewrites
     * the placeholders in the template using the actual values. The
     * placeholders should use the format "{{name}}".
     */
    public static String rewriteTemplate(String templatePath, Map<String, String> placeholders) {
        String template = loadResourceFile(templatePath);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            template = template.replace(entry.getKey(), entry.getValue());
        }
        return template;
    }

    public static void copyDirectory(File source, File target) {
        try (Stream<Path> stream = Files.walk(source.toPath())) {
            for (Path childPath : stream.toList()) {
                Path relativePath = source.toPath().relativize(childPath);
                Path targetPath = target.toPath().resolve(relativePath);
                Files.copy(childPath, targetPath, StandardCopyOption.COPY_ATTRIBUTES);
            }
        } catch(IOException e) {
            throw new RuntimeException("Failed to copy directory", e);
        }
    }
}
