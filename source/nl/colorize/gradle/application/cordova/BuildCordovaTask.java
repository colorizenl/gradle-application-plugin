//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.cordova;

import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class BuildCordovaTask extends DefaultTask {

    private static final String CONFIG_TEMPLATE_FILE = "config.xml";

    @TaskAction
    public void run() {
        ExtensionContainer ext = getProject().getExtensions();
        CordovaExt config = ext.getByType(CordovaExt.class);
        run(config);
    }

    protected void run(CordovaExt config) {
        checkConfiguration(config);

        try {
            File outputDir = config.prepareOutputDir(getProject());
            File configFile = new File(outputDir, "config.xml");

            if (!configFile.exists()) {
                createCordovaApp(config);

                if (config.getPlatformList().contains("ios")) {
                    generateIconIOS(config);
                }
            }

            updateConfig(config);
            buildCordovaApp(config);
        } catch (IOException e) {
            throw new RuntimeException("Unable to build Cordova app", e);
        }
    }

    private void checkConfiguration(CordovaExt config) {
        checkConfiguration("webAppDir", config.getWebAppDir());
        checkConfiguration("appId", config.getAppId());
        checkConfiguration("appName", config.getAppName());
        checkConfiguration("version", config.getDisplayVersion());
        checkConfiguration("icon", config.getIcon());
        checkConfiguration("buildJson", config.getBuildJson());
    }

    private void checkConfiguration(String name, String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException("Missing required configuration option '" + name + "'");
        }
    }

    private void createCordovaApp(CordovaExt config) throws IOException {
        File outputDir = config.prepareOutputDir(getProject());
        outputDir.mkdir();

        runCordova(config, "cordova", "create", outputDir.getAbsolutePath(), config.getAppId(),
            config.getAppName());

        File icon = new File(config.getIcon());
        File buildJson = new File(config.getBuildJson());

        Files.copy(icon.toPath(), getOutput(config, "icon.png").toPath());
        Files.copy(buildJson.toPath(), getOutput(config, "build.json").toPath());

        List<String> platformList = new ArrayList<>(List.of("cordova", "platform", "add"));
        platformList.addAll(config.getPlatformList());
        runCordova(config, platformList);

        if (config.getPlatformList().contains("android")) {
            rewriteAndroidConfig(outputDir);
        }
    }

    private void rewriteAndroidConfig(File outputDir) throws IOException {
        File buildFile = new File(outputDir.getAbsolutePath() + "/platforms/android/build.gradle");

        List<String> originalLines = Files.readAllLines(buildFile.toPath(), StandardCharsets.UTF_8);

        try (PrintWriter writer = new PrintWriter(buildFile, StandardCharsets.UTF_8)) {
            for (String line : originalLines) {
                if (line.trim().startsWith("classpath 'com.android.tools.build:gradle:3")) {
                    line = "classpath 'com.android.tools.build:gradle:4.0.0'";
                }
                writer.println(line);
            }
        }

        File appBuildFile = new File(outputDir.getAbsolutePath() + "/platforms/android/app/build.gradle");

        Files.write(appBuildFile.toPath(),
            "android.lintOptions.checkReleaseBuilds = false\n".getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.APPEND);
    }

    private void generateIconIOS(CordovaExt config) {
        File iosIconDir = getOutput(config,
            "platforms/ios/" + config.getAppName() + "/Images.xcassets/AppIcon.appiconset");

        try {
            AppleIconGenerator iconGenerator = new AppleIconGenerator();
            iconGenerator.generateIconSet(new File(config.getIcon()), iosIconDir);
        } catch (IOException e) {
            throw new RuntimeException("Unable to generate iOS icon");
        }
    }

    private void updateConfig(CordovaExt config) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(CONFIG_TEMPLATE_FILE)) {
            byte[] bytes = stream.readAllBytes();
            String xml = new String(bytes, StandardCharsets.UTF_8);
            xml = xml.replace("@@@ID", config.getAppId());
            xml = xml.replace("@@@NAME", config.getAppName());
            xml = xml.replace("@@@VERSION", config.getDisplayVersion());
            xml = xml.replace("@@@BUILDVERSION", config.getBuildVersion());

            File outputDir = config.prepareOutputDir(getProject());
            File configFile = new File(outputDir, "config.xml");
            Files.write(configFile.toPath(), xml.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void buildCordovaApp(CordovaExt config) {
        File outputDir = config.prepareOutputDir(getProject());
        File appRoot = new File(outputDir, "www");

        getProject().delete(appRoot);
        appRoot.mkdir();
        getProject().copy(copy -> {
            copy.from(config.getWebAppDir());
            copy.into(appRoot);
        });

        List<String> command = new ArrayList<>();
        command.add("cordova");
        command.add("build");
        command.addAll(config.getPlatformList());
        if (config.getDist().equals("release")) {
            command.add("--release");
            command.add("--device");
        }

        runCordova(config, command);
    }

    private void runCordova(CordovaExt config, String... command) {
        CordovaRunner runner = new CordovaRunner(getProject(), config);
        runner.run(command);
    }

    private void runCordova(CordovaExt config, List<String> command) {
        runCordova(config, command.toArray(new String[0]));
    }

    private File getOutput(CordovaExt config, String path) {
        File outputDir = config.prepareOutputDir(getProject());
        return new File(outputDir.getAbsolutePath() + "/" + path);
    }
}
