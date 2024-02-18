//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.windowsexe;

import lombok.Data;
import nl.colorize.gradle.application.AppHelper;
import nl.colorize.gradle.application.Validatable;
import nl.colorize.gradle.application.macapplicationbundle.MacApplicationBundleExt;
import org.gradle.api.Project;

import java.io.File;
import java.util.Collections;
import java.util.List;

@Data
public class WindowsStandaloneExt implements Validatable {

    private boolean inherit;
    private String mainJarName;
    private List<String> args;
    private String name;
    private String version;
    private String icon;
    private String supportURL;
    private int memory;
    private String exeFileName;
    private String javaVersion;

    public WindowsStandaloneExt() {
        this.inherit = false;
        this.args = Collections.emptyList();
        this.memory = 2048;
        this.javaVersion = "17";
    }

    public File getExeFile(Project project) {
        String fileName = exeFileName;
        if (exeFileName == null) {
            fileName = mainJarName.replace(".jar", ".exe");
        }
        return new File(project.getBuildDir(), fileName);
    }

    @Override
    public void validate() {
        AppHelper.check(name != null, "Missing exe.name");
        AppHelper.check(version != null, "Missing exe.version");
        AppHelper.check(icon != null, "Missing exe.icon");
        AppHelper.check(icon.endsWith(".ico"), "Windows icon must be a .ico file");
        AppHelper.check(supportURL != null, "Missing exe.supportURL");
        AppHelper.check(mainJarName != null, "Missing exe.mainJarName");
    }

    public void inherit(MacApplicationBundleExt macConfig) {
        name = macConfig.getName();
        version = macConfig.getBundleVersion();
        mainJarName = macConfig.getMainJarName();
    }
}
