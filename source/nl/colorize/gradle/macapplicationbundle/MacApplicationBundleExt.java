//-----------------------------------------------------------------------------
// Gradle Mac Application Bundle Plugin
// Copyright 2010-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.macapplicationbundle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MacApplicationBundleExt {

    private String outputDir;
    private String name;
    private String displayName;
    private String identifier;
    private String description;
    private String version;
    private String shortVersion;
    private String copyright;

    private String icon;
    private String applicationCategory;
    private String minimumSystemVersion;

    private String contentDir;
    private String mainClassName;
    private List<String> modules;

    private String signIdentityApp;
    private String signIdentityInstaller;

    public MacApplicationBundleExt() {
        initDefaults();
    }

    private void initDefaults() {
        outputDir = "build/mac";
        icon = "resources/icon.icns";
        description = "";
        copyright = "Copyright " + new SimpleDateFormat("yyyy").format(new Date());
        version = "1.0";
        applicationCategory = "public.app-category.developer-tools";
        minimumSystemVersion = "10.13";

        modules = new ArrayList<>();
        modules.add("java.base");
        modules.add("java.logging");
        modules.add("java.desktop");
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getShortVersion() {
        return shortVersion;
    }

    public void setShortVersion(String shortVersion) {
        this.shortVersion = shortVersion;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getApplicationCategory() {
        return applicationCategory;
    }

    public void setApplicationCategory(String applicationCategory) {
        this.applicationCategory = applicationCategory;
    }

    public String getMinimumSystemVersion() {
        return minimumSystemVersion;
    }

    public void setMinimumSystemVersion(String minimumSystemVersion) {
        this.minimumSystemVersion = minimumSystemVersion;
    }

    public String getContentDir() {
        return contentDir;
    }

    public void setContentDir(String contentDir) {
        this.contentDir = contentDir;
    }

    public String getMainClassName() {
        return mainClassName;
    }

    public void setMainClassName(String mainClassName) {
        this.mainClassName = mainClassName;
    }

    public List<String> getModules() {
        return modules;
    }

    public void setModules(List<String> modules) {
        this.modules = modules;
    }

    public String getSignIdentityApp() {
        return signIdentityApp;
    }

    public void setSignIdentityApp(String signIdentityApp) {
        this.signIdentityApp = signIdentityApp;
    }

    public String getSignIdentityInstaller() {
        return signIdentityInstaller;
    }

    public void setSignIdentityInstaller(String signIdentityInstaller) {
        this.signIdentityInstaller = signIdentityInstaller;
    }
}
