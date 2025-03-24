//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.xcode;

import lombok.Data;
import nl.colorize.gradle.application.AppHelper;
import nl.colorize.gradle.application.Validatable;

@Data
public class XcodeGenExt implements Validatable {

    private String appId;
    private String bundleId;
    private String appName;
    private String bundleVersion;
    private String icon;
    private String iconBackgroundColor;
    private String deploymentTarget;
    private String resourcesDir;
    private String launchScreenColor;
    private String outputDir;
    private String xcodeGenPath;

    public XcodeGenExt() {
        this.iconBackgroundColor = "#000000";
        this.outputDir = "xcode";
        this.deploymentTarget = "14.0";
        this.launchScreenColor = "#000000";
        this.xcodeGenPath = "/usr/local/bin/xcodegen";
    }

    @Override
    public void validate() {
        AppHelper.check(appId != null, "Missing xcodeGen.appId");
        AppHelper.check(bundleId != null, "Missing xcodeGen.bundleId");
        AppHelper.check(appName != null, "Missing xcodeGen.appName");
        AppHelper.check(bundleVersion != null, "Missing xcodeGen.bundleVersion");
        AppHelper.check(icon != null, "Missing xcodeGen.icon");
        AppHelper.check(resourcesDir != null, "Missing xcodeGen.resourcesDir");

        AppHelper.check(!appId.contains(" "), "App ID cannot contain spaces");
    }

    public String getBuildVersion() {
        return System.getProperty("buildversion", bundleVersion);
    }
}
