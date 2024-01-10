//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.macapplicationbundle;

import com.oracle.appbundler.AppBundlerTask;
import com.oracle.appbundler.Architecture;
import com.oracle.appbundler.Argument;
import com.oracle.appbundler.JLink;
import com.oracle.appbundler.JMod;
import com.oracle.appbundler.Option;
import com.oracle.appbundler.Runtime;
import nl.colorize.gradle.application.AppHelper;
import org.apache.tools.ant.types.FileSet;
import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CreateApplicationBundleTask extends DefaultTask {

    @TaskAction
    public void run() {
        AppHelper.requireMac();
        ExtensionContainer ext = getProject().getExtensions();
        MacApplicationBundleExt config = ext.getByType(MacApplicationBundleExt.class);
        run(config);
    }

    protected void run(MacApplicationBundleExt config) {
        config.validate();

        File jdk = new File(config.getJdkPath());
        File outputDir = config.getOutputDir(getProject());
        AppHelper.cleanDirectory(outputDir);
        bundle(config, jdk, outputDir);
        if (config.isExtractNatives()) {
            extractNativeLibraries(outputDir);
        }
    }

    private void bundle(MacApplicationBundleExt config, File jdk, File outputDir) {
        AppBundlerTask task = new AppBundlerTask();
        task.setProject(getProject().getAnt().getAntProject());
        task.setOutputDirectory(outputDir);
        task.setName(config.getName());
        task.setDisplayName(getDisplayName(config));
        task.setIdentifier(config.getIdentifier());
        task.setDescription(config.getDescription());
        task.setVersion(getVersion(config));
        task.setShortVersion(getShortVersion(config));
        task.setCopyright(config.getCopyright());
        task.setIcon(new File(config.getIcon()));
        if (config.getApplicationCategory() != null) {
            task.setApplicationCategory(config.getApplicationCategory());
        }
        task.setMinimumSystemVersion(config.getMinimumSystemVersion());
        task.setMainClassName(config.getMainClassName());
        config.getArchitectures().forEach(arch -> task.addConfiguredArch(toArch(arch)));
        task.addConfiguredClassPath(createClassPath(config));
        getCombinedOptions(config).forEach(option -> task.addConfiguredOption(createOption(option)));
        config.getArgs().forEach(arg -> task.addConfiguredArgument(createArg(arg)));
        if (config.getModules().isEmpty()) {
            task.addConfiguredRuntime(createRuntime(jdk));
        } else {
            task.addConfiguredJLink(createJLink(config, jdk));
        }
        task.perform();
    }

    private List<String> getCombinedOptions(MacApplicationBundleExt config) {
        List<String> combinedOptions = new ArrayList<>();
        combinedOptions.add("-Xdock:name='" + getDisplayName(config) + "'");
        combinedOptions.add("-Xdock:icon='Contents/Resources/icon.icns'");
        if (config.isStartOnFirstThread()) {
            combinedOptions.add("-XstartOnFirstThread");
        }
        combinedOptions.addAll(config.getOptions());
        return combinedOptions;
    }

    private FileSet createClassPath(MacApplicationBundleExt config) {
        FileSet classPath = new FileSet();
        classPath.setDir(getContentDir(config));
        classPath.setExcludes("*-sources.jar,*-javadoc.jar");
        return classPath;
    }

    private File getContentDir(MacApplicationBundleExt config) {
        if (config.getContentDir() != null) {
            return new File(config.getContentDir());
        } else {
            return AppHelper.getLibsDir(getProject());
        }
    }

    private Option createOption(String value) {
        Option option = new Option();
        option.setValue(value);
        return option;
    }

    private Argument createArg(String value) {
        Argument arg = new Argument();
        arg.setValue(value);
        return arg;
    }

    private JLink createJLink(MacApplicationBundleExt config, File jdk) {
        JLink jLink = new JLink();
        jLink.setRuntime(jdk.getAbsolutePath());

        List<String> combinedModules = new ArrayList<>();
        combinedModules.addAll(config.getModules());
        combinedModules.addAll(config.getAdditionalModules());

        for (String module : combinedModules) {
            JMod jModule = new JMod();
            jModule.setName(module);
            jLink.addConfiguredJMod(jModule);
        }

        jLink.addConfiguredArgument(createArg("--compress=2"));
        jLink.addConfiguredArgument(createArg("--release-info=" + jdk.getAbsolutePath() + "/release"));

        return jLink;
    }

    private Runtime createRuntime(File jdk) {
        Runtime runtime = new Runtime();
        runtime.setDir(jdk);
        return runtime;
    }

    private Architecture toArch(String arch) {
        Architecture result = new Architecture();
        result.setName(arch);
        return result;
    }

    private String getDisplayName(MacApplicationBundleExt config) {
        String displayName = config.getDisplayName();
        if (displayName == null) {
            displayName = config.getName();
        }
        return displayName;
    }

    private String getVersion(MacApplicationBundleExt config) {
        if (System.getProperty("buildversion") != null) {
            return System.getProperty("buildversion");
        }
        return config.getBundleVersion();
    }

    private String getShortVersion(MacApplicationBundleExt config) {
        if (System.getProperty("shortversion") != null) {
            return System.getProperty("shortversion");
        }
        return config.getBundleVersion();
    }

    /**
     * Extracts all embedded native libraries from JAR files, as extracting
     * at runtime is not allowed by the Mac App Store.
     */
    private void extractNativeLibraries(File outputDir) {
        try {
            Files.walk(outputDir.toPath())
                .map(Path::toFile)
                .filter(file -> file.getName().endsWith(".jar"))
                .filter(file -> file.getParentFile().getName().equals("Java"))
                .forEach(this::extractNativeLibrariesFromJAR);
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract native libraries", e);
        }
    }

    private void extractNativeLibrariesFromJAR(File jarFile) {
        File outputDir = jarFile.getParentFile();

        try (JarFile jar = new JarFile(jarFile)) {
            jar.stream()
                .filter(entry -> entry.getName().endsWith(".dylib"))
                .forEach(entry -> extractNativeLibrary(jar, entry, outputDir));
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract native libraries from " + jarFile, e);
        }
    }

    private void extractNativeLibrary(JarFile jar, JarEntry entry, File outputDir) {
        File outputFile = new File(outputDir, "native-" + entry.getName().replace("/", "-"));
        if (outputFile.exists()) {
            throw new IllegalStateException("File already exists: " + outputFile);
        }

        try (InputStream stream = jar.getInputStream(entry)) {
            Files.copy(stream, outputFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract " + entry.getName(), e);
        }
    }
}
