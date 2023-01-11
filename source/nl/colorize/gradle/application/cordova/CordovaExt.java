//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.cordova;

import lombok.Data;
import nl.colorize.gradle.application.AppHelper;
import org.gradle.api.Project;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Data
public class CordovaExt {

    private String webAppDir;
    private String outputDir;
    private String platforms;
    private String appId;
    private String appName;
    private String displayVersion;
    private String icon;
    private String buildJson;
    private String dist;

    public CordovaExt() {
        outputDir = "cordova";
        displayVersion = "1.0";
        platforms = "ios,android,osx";
        dist = "release";
    }

    public File getOutputDir(Project project) {
        return AppHelper.getOutputDir(project, outputDir);
    }

    public List<String> getPlatformList() {
        return Arrays.asList(platforms.split(","));
    }

    public String getBuildVersion() {
        return System.getProperty("buildversion", displayVersion.replace(".", ""));
    }
}
