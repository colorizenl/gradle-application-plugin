//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.xcode;

import lombok.Data;
import nl.colorize.gradle.application.AppHelper;

@Data
public class XcodeGenExt {

    private String appId;
    private String bundleId;
    private String appName;
    private String appVersion;
    private String icon;
    private String deploymentTarget;
    private String resourcesDir;
    private String outputDir;
    private String xcodeGenPath;

    public XcodeGenExt() {
        this.outputDir = "xcode";
        this.deploymentTarget = "14.0";
        this.xcodeGenPath = "/usr/local/bin/xcodegen";
    }

    public void validate() {
        AppHelper.check(appId != null, "Missing xcodeGen.appId");
        AppHelper.check(bundleId != null, "Missing xcodeGen.bundleId");
        AppHelper.check(appName != null, "Missing xcodeGen.appName");
        AppHelper.check(appVersion != null, "Missing xcodeGen.appVersion");
        AppHelper.check(icon != null, "Missing xcodeGen.icon");
        AppHelper.check(resourcesDir != null, "Missing xcodeGen.resourcesDir");

        AppHelper.check(!appId.contains(" "), "App ID cannot contain spaces");
    }

    public String getBuildVersion() {
        return System.getProperty("buildversion", appVersion);
    }
}
