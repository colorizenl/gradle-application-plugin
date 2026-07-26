//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
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
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates a Mac application bundle that includes both the application and
 * an embedded Java runtime. This is basically a wrapper around the
 * {@code appbundler} Ant task, so that it can be used in Gradle projects.
 */
public abstract class CreateApplicationBundleTask extends DefaultTask {

    @Nested
    public abstract Property<MacApplicationBundleExt> getExt();

    @TaskAction
    public void run() {
        // Overrides the XML system property to make sure the default
        // implementation is used. This avoids dependency conflicts
        // when *other* Gradle plugins try to change the XML
        // configuration, which then breaks the AppBundler task.
        System.setProperty("javax.xml.stream.XMLOutputFactory",
            "com.sun.xml.internal.stream.XMLOutputFactoryImpl");

        AppHelper.requireMac();
        run(getExt().get());
    }

    protected void run(MacApplicationBundleExt config) {
        File jdk = new File(config.getJdkPath().get());
        File outputDir = config.toOutputDir(config.getOutputDir().get());
        AppHelper.cleanDirectory(outputDir);
        bundle(config, jdk, outputDir);

        for (String binary : config.getAdditionalBinaries().get()) {
            copyBinaryFile(new File(binary), config);
        }
    }

    private void bundle(MacApplicationBundleExt config, File jdk, File outputDir) {
        AppBundlerTask task = new AppBundlerTask();
        task.setProject(getProject().getAnt().getAntProject());
        task.setOutputDirectory(outputDir);
        task.setName(config.getName().get());
        task.setDisplayName(getDisplayName(config));
        task.setIdentifier(config.getIdentifier().get());
        task.setDescription(config.getDescription().get());
        task.setVersion(System.getProperty("buildversion", config.getBundleVersion().get()));
        task.setShortVersion(System.getProperty("shortversion", config.getBundleVersion().get()));
        task.setCopyright(config.getCopyright().get());
        task.setIcon(config.toProjectFile(config.getIcon().get()));
        task.setApplicationCategory(config.getApplicationCategory().get());
        task.setMinimumSystemVersion(config.getMinimumSystemVersion().get());
        task.setMainClassName(config.getMainClassName().get());
        config.getArchitectures().get().forEach(arch -> task.addConfiguredArch(toArch(arch)));
        task.addConfiguredClassPath(createClassPath(config));
        getCombinedOptions(config).forEach(option -> task.addConfiguredOption(createOption(option)));
        config.getArgs().get().forEach(arg -> task.addConfiguredArgument(createArg(arg)));
        task.addConfiguredJLink(createJLink(config, jdk));
        task.perform();
    }

    private List<String> getCombinedOptions(MacApplicationBundleExt config) {
        List<String> combinedOptions = new ArrayList<>();
        combinedOptions.add("-Xdock:name='" + getDisplayName(config) + "'");
        combinedOptions.add("-Xdock:icon='Contents/Resources/icon.icns'");
        combinedOptions.addAll(config.getOptions().get());
        return combinedOptions;
    }

    private FileSet createClassPath(MacApplicationBundleExt config) {
        File contentDir = getContentDir(config);
        List<File> fatJAR = AppHelper.walk(contentDir,
            f -> f.getName().endsWith("-all.jar") || f.getName().endsWith("-shadow.jar"));

        FileSet classPath = new FileSet();
        classPath.setDir(contentDir);
        if (!fatJAR.isEmpty()) {
            classPath.setIncludes("*-all.jar,*-shadow.jar");
        }
        classPath.setExcludes("*-sources.jar,*-javadoc.jar");
        return classPath;
    }

    private File getContentDir(MacApplicationBundleExt config) {
        if (config.getContentDir().get().isEmpty()) {
            return config.locateLibsDir();
        }
        return config.toProjectFile(config.getContentDir().get());
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
        combinedModules.addAll(config.getModules().get());
        combinedModules.addAll(config.getAdditionalModules().get());

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
        String displayName = config.getDisplayName().get();
        if (displayName.isEmpty()) {
            displayName = config.getName().get();
        }
        return displayName;
    }

    private void copyBinaryFile(File binaryFile, MacApplicationBundleExt config) {
        try {
            File nativesDir = config.locateNativesDir();
            File outputFile = new File(nativesDir, binaryFile.getName());
            Files.copy(binaryFile.toPath(), outputFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy " + binaryFile, e);
        }
    }
}
