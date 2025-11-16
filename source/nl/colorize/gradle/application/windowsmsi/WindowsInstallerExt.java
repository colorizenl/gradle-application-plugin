//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.windowsmsi;

import lombok.Getter;
import lombok.Setter;
import nl.colorize.gradle.application.AppHelper;
import nl.colorize.gradle.application.Validatable;
import nl.colorize.gradle.application.macapplicationbundle.MacApplicationBundleExt;
import org.gradle.api.Project;

import java.io.File;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class WindowsInstallerExt implements Validatable {

    private boolean inherit;
    private String mainJarName;
    private String mainClassName;
    private List<String> options;
    private List<String> args;
    private String name;
    private String version;
    private String vendor;
    private String description;
    private String copyright;
    private String icon;
    private String uuid;
    private String outputDir;

    public WindowsInstallerExt() {
        this.inherit = false;
        this.options = Collections.emptyList();
        this.args = Collections.emptyList();
        this.outputDir = "windows-msi";
    }

    public File getOutputDir(Project project) {
        return AppHelper.getOutputDir(project, outputDir);
    }

    @Override
    public void validate() {
        AppHelper.check(mainJarName != null, "Missing msi.mainJarName");
        AppHelper.check(mainClassName != null, "Missing msi.mainClassName");
        AppHelper.check(name != null, "Missing msi.name");
        AppHelper.check(version != null, "Missing msi.version");
        AppHelper.check(vendor != null, "Missing msi.vendor");
        AppHelper.check(description != null, "Missing msi.description");
        AppHelper.check(copyright != null, "Missing msi.copyright");
        AppHelper.check(icon != null, "Missing msi.icon");
        AppHelper.check(icon.endsWith(".ico"), "Windows icon must be a .ico file");
        AppHelper.check(uuid != null, "Missing msi.uuid");
    }

    public void inherit(MacApplicationBundleExt macConfig) {
        mainJarName = macConfig.getMainJarName();
        mainClassName = macConfig.getMainClassName();
        name = macConfig.getName();
        version = macConfig.getBundleVersion();
        description = macConfig.getDescription();
        copyright = macConfig.getCopyright();
    }
}
