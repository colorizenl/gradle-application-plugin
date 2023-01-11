//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.windows;

import nl.colorize.gradle.application.AppHelper;
import nl.colorize.gradle.application.macapplicationbundle.MacApplicationBundleExt;
import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskAction;

import java.util.List;

public class PackageMSITask extends DefaultTask {

    @TaskAction
    public void run() {
        AppHelper.requireWindows();

        ExtensionContainer ext = getProject().getExtensions();
        WindowsExt config = ext.getByType(WindowsExt.class);
        MacApplicationBundleExt macConfig = ext.getByType(MacApplicationBundleExt.class);

        if (config.isInherit()) {
            config.inherit(macConfig);
        }

        config.validate();

        List<String> packageCommand = buildPackageCommand(config);
        getProject().exec(exec -> exec.commandLine(packageCommand));
    }

    protected List<String> buildPackageCommand(WindowsExt config) {
        return List.of(
            "jpackage",
            "--type", "msi",
            "--input", AppHelper.getLibsDir(getProject()).getAbsolutePath(),
            "--main-jar", config.getMainJarName(getProject()),
            "--main-class", config.getMainClassName(),
            "--name", config.getName(),
            "--app-version", config.getVersion(),
            "--description", config.getDescription(),
            "--copyright", config.getCopyright(),
            "--icon", config.getIcon(),
            "--win-upgrade-uuid", config.getUuid(),
            "--win-per-user-install",
            "--win-menu",
            "--win-shortcut",
            "--dest", config.getOutputDir(getProject()).getAbsolutePath()
        );
    }
}
