//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.macapplicationbundle;

import lombok.Data;
import nl.colorize.gradle.application.AppHelper;
import org.gradle.api.Project;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Data
public class MacApplicationBundleExt {

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
    private String mainClassName;
    private List<String> modules;
    private List<String> options;
    private boolean startOnFirstThread;
    private String jdkPath;
    private String outputDir;

    public static final List<String> SUPPORTED_EMBEDDED_JDKS = List.of(
        "temurin-17.jdk",
        "temurin-m1-17.jdk",
        "adoptopenjdk-11.jdk"
    );

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
        
        applicationCategory = "public.app-category.developer-tools";
        minimumSystemVersion = "10.13";
        architectures = List.of("arm64", "x86_64");
        
        modules = DEFAULT_MODULES;
        options = List.of("-Xmx2g");
        startOnFirstThread = false;

        jdkPath = AppHelper.getEnvironmentVariable("JAVA_HOME");
        outputDir = "mac";
    }

    public File getOutputDir(Project project) {
        return AppHelper.getOutputDir(project, outputDir);
    }

    public void validate() {
        AppHelper.check(name != null, "Missing macApplicationBundle.name");
        AppHelper.check(identifier != null, "Missing macApplicationBundle.identifier");
        AppHelper.check(bundleVersion != null, "Missing macApplicationBundle.bundleVersion");
        AppHelper.check(mainClassName != null, "Missing macApplicationBundle.mainClassName");

        File jdk = new File(jdkPath);
        AppHelper.check(jdk.exists(), "JDK not found: " + jdk.getAbsolutePath());
        AppHelper.check(jdk.getName().equals("Home"), "JDK should point to /Contents/Home");
    }
}
