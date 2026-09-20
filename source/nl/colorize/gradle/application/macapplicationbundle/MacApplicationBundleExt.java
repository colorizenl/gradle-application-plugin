//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.macapplicationbundle;

import nl.colorize.gradle.application.AppHelper;
import nl.colorize.gradle.application.ApplicationExt;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface MacApplicationBundleExt extends ApplicationExt {

    @Input Property<String> getName();
    @Input Property<String> getDisplayName();
    @Input Property<String> getIdentifier();
    @Input Property<String> getDescription();
    @Input Property<String> getBundleVersion();
    @Input Property<String> getCopyright();
    @Input Property<String> getIcon();
    @Input Property<String> getApplicationCategory();
    @Input Property<String> getMinimumSystemVersion();
    @Input ListProperty<String> getArchitectures();
    @Input Property<String> getContentDir();
    @Input Property<String> getMainJarName();
    @Input Property<String> getMainClassName();
    @Input ListProperty<String> getModules();
    @Input ListProperty<String> getAdditionalModules();
    @Input ListProperty<String> getOptions();
    @Input ListProperty<String> getArgs();
    @Input Property<String> getJdkPath();
    @Input Property<Boolean> getSignNativeLibraries();
    @Input ListProperty<String> getAdditionalBinaries();
    @Input Property<String> getOutputDir();

    @Override
    default void init(Project project) {
        getProjectDirRef().set(project.getProjectDir());
        getBuildDirRef().set(project.getLayout().getBuildDirectory().getAsFile().get());
        getDisplayName().convention("");
        getIcon().convention("resources/icon.icns");
        getDescription().convention("");
        getCopyright().convention("Copyright " + new SimpleDateFormat("yyyy").format(new Date()));
        getBundleVersion().convention("1.0");
        getApplicationCategory().convention("public.app-category.utilities");
        getMinimumSystemVersion().convention("10.13");
        getArchitectures().convention(List.of("arm64", "x86_64"));
        getContentDir().convention("");
        getModules().convention(DEFAULT_MODULES);
        getAdditionalModules().convention(Collections.emptyList());
        getOptions().convention(List.of("-Xmx2g"));
        getArgs().convention(Collections.emptyList());
        getJdkPath().convention(Optional.ofNullable(System.getenv("EMBEDDED_JAVA_HOME"))
            .orElse(AppHelper.getEnvironmentVariable("JAVA_HOME")));
        getSignNativeLibraries().convention(false);
        getAdditionalBinaries().convention(Collections.emptyList());
        getOutputDir().convention("mac");
    }

    default File locateApplicationBundle() {
        File outputDir = toOutputDir(getOutputDir().get());
        return new File(outputDir, getName().get() + ".app");
    }

    default File locateEmbeddedJDK() {
        File appBundleDir = locateApplicationBundle();
        return locateEmbeddedJDK(appBundleDir);
    }

    default File locateEmbeddedJDK(File appBundleDir) {
        File pluginsDir = new File(appBundleDir, "Contents/PlugIns");
        File[] files = pluginsDir.listFiles(File::isDirectory);

        if (files == null) {
            throw new RuntimeException("Failed to list " + pluginsDir.getAbsolutePath());
        }

        for (File pluginDir : files) {
            String plugin = pluginDir.getName();
            if (plugin.startsWith("jdk-") || plugin.startsWith("temurin-") || plugin.endsWith(".jdk")) {
                return pluginDir;
            }
        }

        throw new RuntimeException("Cannot locate embedded JDK: " + appBundleDir.getAbsolutePath());
    }

    default File locateNativesDir() {
        File appBundleDir = locateApplicationBundle();
        return new File(appBundleDir, "Contents/MacOS");
    }

    public static final String SIGN_APP_ENV = "MAC_SIGN_APP_IDENTITY";
    public static final String SIGN_INSTALLER_ENV = "MAC_SIGN_INSTALLER_IDENTITY";
    public static final List<String> DEFAULT_MODULES = List.of(
        "java.base",
        "java.desktop",
        "java.logging",
        "java.net.http",
        "java.sql",
        "jdk.crypto.ec"
    );
}
