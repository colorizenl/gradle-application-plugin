//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.windows;

import nl.colorize.gradle.application.AppHelper;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public abstract class PackageMSITask extends DefaultTask {

    @Nested
    public abstract Property<WindowsInstallerExt> getExt();

    @Inject
    public abstract ExecOperations getExecService();

    @TaskAction
    public void run() {
        AppHelper.requireWindows();
        List<String> packageCommand = buildPackageCommand(getExt().get());
        getExecService().exec(exec -> exec.commandLine(packageCommand));
    }

    protected List<String> buildPackageCommand(WindowsInstallerExt config) {
        List<String> command = new ArrayList<>();
        command.add("jpackage");
        command.add("--type");
        command.add("msi");
        command.add("--input");
        command.add(config.locateLibsDir().getAbsolutePath());
        command.add("--main-jar");
        command.add(config.getMainJarName().get());
        command.add("--main-class");
        command.add(config.getMainClassName().get());
        command.add("--name");
        command.add(config.getName().get());
        command.add("--app-version");
        command.add(config.getVersion().get());
        command.add("--description");
        command.add(config.getDescription().get());
        command.add("--copyright");
        command.add(config.getCopyright().get());
        command.add("--icon");
        command.add(config.toProjectFile(config.getIcon().get()).getAbsolutePath());
        command.add("--win-upgrade-uuid");
        command.add(config.getUuid().get());
        command.add("--win-per-user-install");
        command.add("--win-menu");
        command.add("--win-shortcut");
        command.add("--dest");
        command.add(config.toOutputDir(config.getOutputDir().get()).getAbsolutePath());
        for (String option : config.getOptions().get()) {
            command.add("--java-options");
            command.add(option);
        }
        for (String arg : config.getArgs().get()) {
            command.add("--arguments");
            command.add(arg);
        }
        return command;
    }
}
