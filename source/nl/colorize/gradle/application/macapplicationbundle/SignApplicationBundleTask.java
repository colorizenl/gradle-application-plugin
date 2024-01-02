//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.macapplicationbundle;

import nl.colorize.gradle.application.AppHelper;
import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SignApplicationBundleTask extends DefaultTask {

    private static final String ENTITLEMENTS_APP = "entitlements-app.plist";
    private static final String ENTITLEMENTS_JRE = "entitlements-jre.plist";

    @TaskAction
    public void run() {
        AppHelper.requireMac();

        ExtensionContainer ext = getProject().getExtensions();
        MacApplicationBundleExt config = ext.getByType(MacApplicationBundleExt.class);

        try {
            run(config);
        } catch (IOException e) {
            throw new RuntimeException("Unable to sign application bundle", e);
        }
    }

    protected void run(MacApplicationBundleExt config) throws IOException {
        File appBundle = new File(config.getOutputDir(getProject()), config.getName() + ".app");

        if (!appBundle.exists()) {
            throw new IllegalStateException("Application bundle does not exist: " +
                appBundle.getAbsolutePath());
        }

        File pluginsDir = new File(appBundle.getAbsolutePath() + "/Contents/PlugIns");
        File embeddedJDK = new File(pluginsDir, getEmbeddedJdkName());

        if (!embeddedJDK.exists()) {
            throw new IllegalStateException("Cannot locate embedded JDK: " +
                embeddedJDK.getAbsolutePath());
        }

        File jreEntitlements = generateEntitlements(ENTITLEMENTS_JRE);

        Files.walk(appBundle.toPath())
            .map(Path::toFile)
            .filter(this::shouldSignFile)
            .forEach(bin -> sign(bin, jreEntitlements));

        sign(embeddedJDK, jreEntitlements);
        sign(appBundle, generateEntitlements(ENTITLEMENTS_APP));
        createInstallerPackage(config, appBundle);
    }

    private String getEmbeddedJdkName() {
        String javaHome = System.getenv("JAVA_HOME");

        return MacApplicationBundleExt.SUPPORTED_EMBEDDED_JDKS.stream()
            .filter(javaHome::contains)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported JDK: " + javaHome));
    }

    private boolean shouldSignFile(File file) {
        return file.getName().endsWith(".dylib") || file.getName().equals("jspawnhelper");
    }

    private void sign(File target, File entitlements) {
        exec(
            "codesign",
            "-s", AppHelper.getEnvironmentVariable(MacApplicationBundleExt.SIGN_APP_ENV),
            "-vvvv",
            "--force",
            "--entitlements", entitlements.getAbsolutePath(),
            target.getAbsolutePath()
        );
    }

    private void createInstallerPackage(MacApplicationBundleExt config, File appFile) {
        File pkgFile = new File(config.getOutputDir(getProject()), config.getName() + ".pkg");

        exec(
            "productbuild",
            "--component", appFile.getAbsolutePath(),
            "/Applications",
            "--sign", AppHelper.getEnvironmentVariable(MacApplicationBundleExt.SIGN_INSTALLER_ENV),
            pkgFile.getAbsolutePath()
        );
    }

    private void exec(String... command) {
        List<String> args = List.of(command);
        getProject().exec(exec -> exec.commandLine(args));
    }

    private File generateEntitlements(String sourceFile) throws IOException {
        File tempFile = File.createTempFile("entitlements-" + System.currentTimeMillis(), ".plist");

        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(sourceFile)) {
            byte[] contents = stream.readAllBytes();
            Files.write(tempFile.toPath(), contents);
        }

        return tempFile;
    }
}
