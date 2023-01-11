//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.windows;

import lombok.Data;
import nl.colorize.gradle.application.AppHelper;
import nl.colorize.gradle.application.macapplicationbundle.MacApplicationBundleExt;
import org.gradle.api.Project;

import java.io.File;

@Data
public class WindowsExt {

    private boolean inherit;
    private String mainJarName;
    private String mainClassName;
    private String name;
    private String version;
    private String vendor;
    private String description;
    private String copyright;
    private String icon;
    private String uuid;
    private String outputDir;

    public WindowsExt() {
        this.inherit = false;
        this.outputDir = "windows";
    }

    public String getMainJarName(Project project) {
        if (mainJarName != null) {
            return mainJarName;
        }
        return AppHelper.getJarFileName(project);
    }

    public File getOutputDir(Project project) {
        return AppHelper.getOutputDir(project, outputDir);
    }

    public void validate() {
        AppHelper.check(mainJarName != null, "Missing windows.mainJarName");
        AppHelper.check(mainClassName != null, "Missing windows.mainClassName");
        AppHelper.check(name != null, "Missing windows.name");
        AppHelper.check(version != null, "Missing windows.version");
        AppHelper.check(vendor != null, "Missing windows.vendor");
        AppHelper.check(description != null, "Missing windows.description");
        AppHelper.check(copyright != null, "Missing windows.copyright");
        AppHelper.check(icon != null, "Missing windows.icon");
        AppHelper.check(uuid != null, "Missing windows.uuid");

        AppHelper.check(icon.endsWith(".ico"), "Windows icon must be a .ico file");
    }

    public void inherit(MacApplicationBundleExt macConfig) {
        mainClassName = macConfig.getMainClassName();
        name = macConfig.getName();
        version = macConfig.getBundleVersion();
        description = macConfig.getDescription();
        copyright = macConfig.getCopyright();
    }
}
