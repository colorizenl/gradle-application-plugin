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
import nl.colorize.gradle.application.AppHelper;
import org.apache.tools.ant.types.FileSet;
import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Creates a Mac application bundle that includes both the application and
 * an embedded Java runtime. This is basically a wrapper around the
 * {@code appbundler} Ant task, so that it can be used in Gradle projects.
 */
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

        if (config.getLauncher().equals("shell")) {
            generateShellLauncher(config, jdk);
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
        task.setVersion(System.getProperty("buildversion", config.getBundleVersion()));
        task.setShortVersion(System.getProperty("shortversion", config.getBundleVersion()));
        task.setCopyright(config.getCopyright());
        task.setIcon(new File(config.getIcon()));
        task.setApplicationCategory(config.getApplicationCategory());
        task.setMinimumSystemVersion(config.getMinimumSystemVersion());
        task.setMainClassName(config.getMainClassName());
        config.getArchitectures().forEach(arch -> task.addConfiguredArch(toArch(arch)));
        task.addConfiguredClassPath(createClassPath(config));
        getCombinedOptions(config).forEach(option -> task.addConfiguredOption(createOption(option)));
        config.getArgs().forEach(arg -> task.addConfiguredArgument(createArg(arg)));
        task.addConfiguredJLink(createJLink(config, jdk));
        task.perform();
    }

    private List<String> getCombinedOptions(MacApplicationBundleExt config) {
        List<String> combinedOptions = new ArrayList<>();
        combinedOptions.add("-Xdock:name='" + getDisplayName(config) + "'");
        combinedOptions.add("-Xdock:icon='Contents/Resources/icon.icns'");
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

    /**
     * Generates a Shell script that launches the application. This will then
     * be used instead of the normal native launcher executable, which has
     * compatibility problems with some applications.
     */
    private void generateShellLauncher(MacApplicationBundleExt config, File jdk) {
        File appBundle = config.locateApplicationBundle(getProject());
        Path embeddedJDK = config.locateEmbeddedJDK(getProject()).toPath();

        Map<String, String> launcherProperties = Map.of(
            "{{jdk}}", embeddedJDK.getFileName().toString(),
            "{{jarFileName}}", config.getMainJarName(),
            "{{appName}}", config.getName(),
            "{{appArgs}}", String.join(" ", config.getArgs())
        );

        try {
            File launcher = new File(appBundle, "/Contents/MacOS/ColorizeLauncher");
            String template = AppHelper.rewriteTemplate("launcher.sh", launcherProperties);
            Files.writeString(launcher.toPath(), template, UTF_8);
            launcher.setExecutable(true, false);

            File plistFile = new File(appBundle, "/Contents/Info.plist");
            String plist = Files.readString(plistFile.toPath(), UTF_8);
            plist = plist.replace("<string>JavaAppLauncher</string>", "<string>ColorizeLauncher</string>");
            Files.writeString(plistFile.toPath(), plist, UTF_8);

            // JavaAppLauncher doesn't need the Java binary,
            // but the shell script does.
            Files.createDirectory(embeddedJDK.resolve("Contents/Home/bin"));
            Files.copy(jdk.toPath().resolve("bin/java"), embeddedJDK.resolve("Contents/Home/bin/java"));

            File nativeLauncher = new File(appBundle, "/Contents/MacOS/JavaAppLauncher");
            nativeLauncher.delete();
        } catch (IOException e) {
            throw new RuntimeException("Error while generating shell launcher", e);
        }
    }
}
