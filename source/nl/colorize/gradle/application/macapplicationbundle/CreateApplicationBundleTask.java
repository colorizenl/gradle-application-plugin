//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.macapplicationbundle;

import com.oracle.appbundler.AppBundlerTask;
import com.oracle.appbundler.Argument;
import com.oracle.appbundler.JLink;
import com.oracle.appbundler.JMod;
import com.oracle.appbundler.Option;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.List;

public class CreateApplicationBundleTask extends DefaultTask {

    @TaskAction
    public void run() {
        ExtensionContainer ext = getProject().getExtensions();
        MacApplicationBundleExt config = ext.getByType(MacApplicationBundleExt.class);
        run(config);
    }

    protected void run(MacApplicationBundleExt config) {
        File outputDir = prepareOutputDir(config);
        File appBundle = new File(outputDir, config.getName() + ".app");

        createApplicationBundle(config, outputDir);

        if (!appBundle.exists()) {
            throw new IllegalStateException("Application bundle not found at " +
                appBundle.getAbsolutePath());
        }

        if (config.getPluginDirs() != null) {
            embedPlugins(config.getPluginDirs(), appBundle);
        }
    }

    private void createApplicationBundle(MacApplicationBundleExt config, File outputDir) {
        AppBundlerTask task = new AppBundlerTask();
        task.setProject(createAntProject());

        task.setOutputDirectory(outputDir);
        task.setName(get("name", config.getName()));
        task.setDisplayName(getDisplayName(config));
        task.setIdentifier(get("identifier", config.getIdentifier()));
        task.setDescription(get("description", config.getDescription(), getDisplayName(config)));
        task.setVersion(getVersion(config));
        task.setShortVersion(getShortVersion(config));
        task.setCopyright(get("copyright", config.getCopyright()));
        task.setIcon(new File(get("icon", config.getIcon())));
        task.setApplicationCategory(get("applicationCategory", config.getApplicationCategory()));
        task.setMinimumSystemVersion(get("minimumSystemVersion", config.getMinimumSystemVersion()));
        task.setMainClassName(get("mainClassName", config.getMainClassName()));

        task.addConfiguredClassPath(createClassPath(config));

        task.addConfiguredOption(createOption("-Xdock:name='" + getDisplayName(config) + "'"));
        task.addConfiguredOption(createOption("-Xdock:icon='Contents/Resources/icon.icns'"));
        for (String option : config.getOptions()) {
            task.addConfiguredOption(createOption(option));
        }
        if (config.isStartOnFirstThread()) {
            task.addConfiguredOption(createOption("-XstartOnFirstThread"));
        }

        task.addConfiguredJLink(createJLink(config));

        task.perform();
    }

    private void embedPlugins(List<String> pluginDirs, File appBundle) {
        File outputDir = new File(appBundle.getAbsolutePath() + "/Contents/PlugIns");

        for (String pluginDir : pluginDirs) {
            File source = new File(pluginDir);
            File target = new File(outputDir, source.getName());

            getProject().copy(copy -> {
                copy.from(source.getAbsolutePath());
                copy.into(target.getAbsolutePath());
            });
        }
    }

    private File prepareOutputDir(MacApplicationBundleExt config) {
        File outputDir = new File(get("outputDir", config.getOutputDir()));

        if (outputDir.getParentFile() != null && !outputDir.getParentFile().exists()) {
            throw new IllegalArgumentException("Invalid output directory");
        }

        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        return outputDir;
    }

    private Project createAntProject() {
        Project project = new Project();
        project.setBaseDir(new File("."));
        return project;
    }

    private FileSet createClassPath(MacApplicationBundleExt config) {
        FileSet classPath = new FileSet();
        classPath.setDir(getContentDir(config));
        classPath.setIncludes("*.jar");
        return classPath;
    }

    private File getContentDir(MacApplicationBundleExt config) {
        if (config.getContentDir() != null) {
            return new File(config.getContentDir());
        }

        File libsDir = (File) getProject().getProperties().get("libsDir");
        if (libsDir != null) {
            return libsDir;
        }

        // Gradle 7 and higher no longer have the libsDir property.

        File buildDir = getProject().getBuildDir();
        String libsDirName = (String) getProject().getProperties().get("libsDirName");
        return new File(buildDir, libsDirName);
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

    private JLink createJLink(MacApplicationBundleExt config) {
        JLink jLink = new JLink();
        jLink.setRuntime(getJavaHome());

        for (String module : config.getModules()) {
            JMod jModule = new JMod();
            jModule.setName(module);
            jLink.addConfiguredJMod(jModule);
        }

        jLink.addConfiguredArgument(createArg("--compress=2"));
        jLink.addConfiguredArgument(createArg("--release-info=" + getJavaHome() + "/release"));

        return jLink;
    }

    private String get(String name, String configValue) {
        if (configValue == null || configValue.isEmpty()) {
            throw new IllegalStateException("Required configuration option " + name + " is missing");
        }
        return configValue;
    }

    private String get(String name, String configValue, String defaultValue) {
        if (configValue == null || configValue.isEmpty()) {
            return defaultValue;
        } else {
            return configValue;
        }
    }

    private String getDisplayName(MacApplicationBundleExt config) {
        String displayName = config.getDisplayName();
        if (displayName == null) {
            displayName = get("name", config.getName());
        }
        return displayName;
    }

    private String getVersion(MacApplicationBundleExt config) {
        if (System.getProperty("buildversion") != null) {
            return System.getProperty("buildversion");
        } else {
            return get("bundleVersion", config.getBundleVersion());
        }
    }

    private String getShortVersion(MacApplicationBundleExt config) {
        if (System.getProperty("shortversion") != null) {
            return System.getProperty("shortversion");
        } else {
            return get("bundleVersion", config.getBundleVersion());
        }
    }

    private String getJavaHome() {
        String javaHome = System.getenv("JAVA_HOME");
        if (javaHome == null) {
            throw new IllegalStateException("Environment variable JAVA_HOME not set");
        }
        return javaHome;
    }
}
