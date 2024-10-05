//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.macapplicationbundle;

import lombok.Data;
import nl.colorize.gradle.application.AppHelper;
import nl.colorize.gradle.application.Validatable;
import org.gradle.api.Project;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Data
public class MacApplicationBundleExt implements Validatable {

    private String name;
    private String displayName;
    private String identifier;
    private String description;
    private String bundleVersion;
    private String copyright;

    private String icon;
    private String applicationCategory;
    private String minimumSystemVersion;
    private List<String> architectures;
    private String contentDir;
    private String mainJarName;
    private String mainClassName;
    private List<String> modules;
    private List<String> additionalModules;
    private List<String> options;
    private List<String> args;
    private String jdkPath;
    private boolean signNativeLibraries;
    private String outputDir;

    public static final String SIGN_APP_ENV = "MAC_SIGN_APP_IDENTITY";
    public static final String SIGN_INSTALLER_ENV = "MAC_SIGN_INSTALLER_IDENTITY";

    private static final List<String> DEFAULT_MODULES = List.of(
        "java.base",
        "java.desktop",
        "java.logging",
        "java.net.http",
        "java.sql",
        "jdk.crypto.ec"
    );

    public MacApplicationBundleExt() {
        icon = "resources/icon.icns";
        description = "";
        copyright = "Copyright " + new SimpleDateFormat("yyyy").format(new Date());
        bundleVersion = "1.0";
        applicationCategory = "public.app-category.utilities";
        
        minimumSystemVersion = "10.13";
        architectures = List.of("arm64", "x86_64");
        modules = DEFAULT_MODULES;
        additionalModules = Collections.emptyList();
        options = List.of("-Xmx2g");
        args = Collections.emptyList();
        jdkPath = Optional.ofNullable(System.getenv("EMBEDDED_JAVA_HOME"))
            .orElse(AppHelper.getEnvironmentVariable("JAVA_HOME"));
        signNativeLibraries = false;
        outputDir = "mac";
    }

    public File getOutputDir(Project project) {
        return AppHelper.getOutputDir(project, outputDir);
    }

    @Override
    public void validate() {
        AppHelper.check(name != null, "Missing macApplicationBundle.name");
        AppHelper.check(identifier != null, "Missing macApplicationBundle.identifier");
        AppHelper.check(bundleVersion != null, "Missing macApplicationBundle.bundleVersion");
        AppHelper.check(mainJarName != null, "Missing macApplicationBundle.mainJarName");
        AppHelper.check(mainClassName != null, "Missing macApplicationBundle.mainClassName");

        File jdk = new File(jdkPath);
        AppHelper.check(jdk.exists(), "JDK not found: " + jdk.getAbsolutePath());
        AppHelper.check(jdk.getName().equals("Home"), "JDK should point to /Contents/Home");
    }

    protected File locateApplicationBundle(Project project) {
        return new File(getOutputDir(project), getName() + ".app");
    }

    protected File locateEmbeddedJDK(Project project) {
        File appBundleDir = locateApplicationBundle(project);
        return locateEmbeddedJDK(appBundleDir);
    }

    protected File locateEmbeddedJDK(File appBundleDir) {
        File pluginsDir = new File(appBundleDir.getAbsolutePath() + "/Contents/PlugIns");

        for (File pluginDir : pluginsDir.listFiles(File::isDirectory)) {
            String plugin = pluginDir.getName();
            if (plugin.startsWith("jdk-") || plugin.startsWith("temurin-")) {
                return pluginDir;
            }
        }

        throw new RuntimeException("Cannot locate embedded JDK: " + appBundleDir.getAbsolutePath());
    }
}
