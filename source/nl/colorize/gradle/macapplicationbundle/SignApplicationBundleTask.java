//-----------------------------------------------------------------------------
// Gradle Mac Application Bundle Plugin
// Copyright 2010-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.macapplicationbundle;

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
        ExtensionContainer ext = getProject().getExtensions();
        MacApplicationBundleExt config = ext.getByType(MacApplicationBundleExt.class);

        try {
            run(config);
        } catch (IOException e) {
            throw new RuntimeException("Unable to sign application bundle", e);
        }
    }

    protected void run(MacApplicationBundleExt config) throws IOException {
        File appFile = new File(config.getOutputDir() + "/" + config.getName() + ".app");

        if (!appFile.exists()) {
            throw new IllegalStateException("Application bundle does not exist: " +
                appFile.getAbsolutePath());
        }

        File embeddedJRE = new File(appFile.getAbsolutePath() + "/Contents/PlugIns/adoptopenjdk-11.jdk");
        File jreEntitlements = generateEntitlements(ENTITLEMENTS_JRE);

        Files.walk(appFile.toPath())
            .map(Path::toFile)
            .filter(file -> file.getName().endsWith(".dylib") || file.getName().equals("jspawnhelper"))
            .forEach(bin -> sign(config, bin, jreEntitlements));

        sign(config, embeddedJRE, jreEntitlements);
        sign(config, appFile, generateEntitlements(ENTITLEMENTS_APP));
        createInstallerPackage(config, appFile);
    }

    private void sign(MacApplicationBundleExt config, File target, File entitlements) {
        String identity = getAppSignIdentity(config);

        exec("codesign",
            "-s", identity,
            "-vvvv",
            "--force",
            "--entitlements", entitlements.getAbsolutePath(),
            target.getAbsolutePath());
    }

    private void createInstallerPackage(MacApplicationBundleExt config, File appFile) {
        File pkgFile = new File(config.getOutputDir() + "/" + config.getName() + ".pkg");
        String identity = getInstallerSignIdentity(config);

        exec("productbuild",
            "--component", appFile.getAbsolutePath(),
            "/Applications",
            "--sign", identity, pkgFile.getAbsolutePath());
    }

    private void exec(String... command) {
        List<String> args = List.of(command);
        getProject().exec(exec -> exec.commandLine(args));
    }

    private String getAppSignIdentity(MacApplicationBundleExt config) {
        String identity = config.getSignIdentityApp();
        if (identity == null) {
            throw new IllegalStateException("Missing signing identity for app");
        }
        return identity;
    }

    private String getInstallerSignIdentity(MacApplicationBundleExt config) {
        String identity = config.getSignIdentityInstaller();
        if (identity == null) {
            throw new IllegalStateException("Missing signing identity for installer");
        }
        return identity;
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
