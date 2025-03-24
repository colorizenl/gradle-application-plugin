//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.macapplicationbundle;

import nl.colorize.gradle.application.AppHelper;
import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Gradle task to create a Mac application bundle using the {@code jpackage}
 * tool that is included with the JDK.
 */
public class PackageApplicationBundleTask extends DefaultTask {

    private ExecOperations execService;

    private static final String ENTITLEMENTS = "entitlements-app.plist";

    @Inject
    public PackageApplicationBundleTask(ExecOperations execService) {
        this.execService = execService;
    }

    @TaskAction
    public void run() {
        AppHelper.requireMac();
        ExtensionContainer ext = getProject().getExtensions();
        MacApplicationBundleExt config = ext.getByType(MacApplicationBundleExt.class);
        run(config);
    }

    protected void run(MacApplicationBundleExt config) {
        File outputDir = config.getOutputDir(getProject());
        AppHelper.cleanDirectory(outputDir);

        execService.exec(exec -> exec.commandLine(getCommand("dmg", config)));
        execService.exec(exec -> exec.commandLine(getCommand("pkg", config)));
    }

    protected List<String> getCommand(String packageType, MacApplicationBundleExt config) {
        List<String> command = new ArrayList<>();
        command.add("jpackage");
        command.add("--type");
        command.add(packageType);
        command.add("--app-version");
        command.add(config.getBundleVersion());
        command.add("--copyright");
        command.add(config.getCopyright());
        command.add("--description");
        command.add(config.getDescription());
        command.add("--icon");
        command.add(new File(config.getIcon()).getAbsolutePath());
        command.add("--name");
        command.add(config.getName());
        command.add("--dest");
        command.add(config.getOutputDir(getProject()).getAbsolutePath());
        command.add("--add-modules");
        command.add(getModules(config));
        command.add("--main-class");
        command.add(config.getMainClassName());
        command.add("--main-jar");
        command.add(config.getMainJarName());
        command.add("--input");
        command.add(config.getContentDir());
        if (!config.getArgs().isEmpty()) {
            command.add("--arguments");
            command.add(String.join(" ", config.getArgs()));
        }
        command.add("--mac-sign");
        command.add("--mac-app-store");
        command.add("--mac-entitlements");
        command.add(generateEntitlements().getAbsolutePath());
        command.add("--mac-signing-key-user-name");
        command.add(AppHelper.getEnvironmentVariable(MacApplicationBundleExt.SIGN_APP_ENV));
        return command;
    }

    private String getModules(MacApplicationBundleExt config) {
        List<String> modules = new ArrayList<>();
        modules.addAll(config.getModules());
        modules.addAll(config.getAdditionalModules());
        return String.join(",", modules);
    }

    private File generateEntitlements() {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(ENTITLEMENTS)) {
            byte[] contents = stream.readAllBytes();
            File tempFile = File.createTempFile("entitlements-" + System.currentTimeMillis(), ".plist");
            Files.write(tempFile.toPath(), contents);
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Error while generating entitlements file", e);
        }
    }
}
