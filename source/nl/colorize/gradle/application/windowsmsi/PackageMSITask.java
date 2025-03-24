//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.windowsmsi;

import nl.colorize.gradle.application.AppHelper;
import nl.colorize.gradle.application.macapplicationbundle.MacApplicationBundleExt;
import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class PackageMSITask extends DefaultTask {

    private ExecOperations execService;

    @Inject
    public PackageMSITask(ExecOperations execService) {
        this.execService = execService;
    }

    @TaskAction
    public void run() {
        AppHelper.requireWindows();

        ExtensionContainer ext = getProject().getExtensions();
        WindowsInstallerExt config = ext.getByType(WindowsInstallerExt.class);
        MacApplicationBundleExt macConfig = ext.getByType(MacApplicationBundleExt.class);

        if (config.isInherit()) {
            config.inherit(macConfig);
        }

        config.validate();

        List<String> packageCommand = buildPackageCommand(config);
        execService.exec(exec -> exec.commandLine(packageCommand));
    }

    protected List<String> buildPackageCommand(WindowsInstallerExt config) {
        List<String> baseCommand = List.of(
            "jpackage",
            "--type", "msi",
            "--input", AppHelper.getLibsDir(getProject()).getAbsolutePath(),
            "--main-jar", config.getMainJarName(),
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

        List<String> command = new ArrayList<>();
        command.addAll(baseCommand);

        for (String option : config.getOptions()) {
            command.add("--java-options");
            command.add(option);
        }

        for (String arg : config.getArgs()) {
            command.add("--arguments");
            command.add(arg);
        }

        return command;
    }
}
