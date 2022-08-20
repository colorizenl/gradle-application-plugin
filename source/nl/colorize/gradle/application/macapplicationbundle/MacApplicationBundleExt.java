//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.macapplicationbundle;

import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class MacApplicationBundleExt {

    private String outputDir;
    private String name;
    private String displayName;
    private String identifier;
    private String description;
    private String bundleVersion;
    private String shortVersion;
    private String copyright;

    private String icon;
    private String applicationCategory;
    private String minimumSystemVersion;

    private String contentDir;
    private String mainClassName;
    private List<String> modules;
    private List<String> options;
    private boolean startOnFirstThread;

    private List<String> pluginDirs;
    private boolean signPlugins;

    private String signIdentityApp;
    private String signIdentityInstaller;
    
    private static final List<String> DEFAULT_MODULES = Arrays.asList(
        "java.base",
        "java.desktop",
        "java.logging",
        "java.net.http",
        "java.sql",
        "jdk.crypto.ec"
    );

    public MacApplicationBundleExt() {
        initDefaults();
    }

    private void initDefaults() {
        outputDir = "build/mac";
        icon = "resources/icon.icns";
        description = "";
        copyright = "Copyright " + new SimpleDateFormat("yyyy").format(new Date());
        bundleVersion = "1.0";
        applicationCategory = "public.app-category.developer-tools";
        minimumSystemVersion = "10.13";

        modules = new ArrayList<>();
        modules.addAll(DEFAULT_MODULES);

        options = new ArrayList<>();
        options.add("-Xmx2g");
        startOnFirstThread = false;

        pluginDirs = new ArrayList<>();
        signPlugins = true;
    }
}
