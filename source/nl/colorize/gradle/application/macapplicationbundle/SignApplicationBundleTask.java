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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Signs the Mac application bundle from {@link CreateApplicationBundleTask}.
 * This is done using the {@code codesign} command line tool that is installed
 * as part of Xcode. This tool, and therefore this Gradle task, is only
 * supported on Mac.
 */
public class SignApplicationBundleTask extends DefaultTask {

    private ExecOperations execService;

    private static final String ENTITLEMENTS_APP = "entitlements-app.plist";
    private static final String ENTITLEMENTS_JRE = "entitlements-jre.plist";

    @Inject
    public SignApplicationBundleTask(ExecOperations execService) {
        this.execService = execService;
    }

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
        File appBundle = config.locateApplicationBundle(getProject());
        File embeddedJDK = config.locateEmbeddedJDK(getProject());
        File appEntitlements = generateEntitlements(ENTITLEMENTS_APP);
        File jreEntitlements = generateEntitlements(ENTITLEMENTS_JRE);

        checkEmbeddedJDK(embeddedJDK);

        if (config.isSignNativeLibraries()) {
            extractNativeLibraries(config);
        }

        for (File file : AppHelper.walk(appBundle, file -> isNativeBinary(file, config))) {
            sign(file, jreEntitlements);
        }

        sign(embeddedJDK, jreEntitlements);
        sign(appBundle, appEntitlements);
        createInstallerPackage(config, appBundle);
    }

    private void checkEmbeddedJDK(File embeddedJDK) {
        File dylib = new File(embeddedJDK, "Contents/MacOS/libjli.dylib");
        List<String> command = List.of("xattr", dylib.getAbsolutePath());
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        execService.exec(exec -> {
            exec.commandLine(command);
            exec.setStandardOutput(output);
        });

        if (output.toString(UTF_8).contains("com.apple.quarantine")) {
            throw new RuntimeException("Embedded JDK contains com.apple.quarantine flag");
        }
    }

    private boolean isNativeBinary(File file, MacApplicationBundleExt config) {
        if (!config.getAdditionalBinaries().isEmpty()) {
            File nativesDir = config.locateNativesDir(getProject());
            if (file.getParentFile().equals(nativesDir)) {
                return true;
            }
        }

        return file.getName().endsWith(".dylib") ||
            file.getName().equals("jspawnhelper");
    }

    private void sign(File target, File entitlements) {
        List<String> command = List.of(
            "codesign",
            "-s", AppHelper.getEnvironmentVariable(MacApplicationBundleExt.SIGN_APP_ENV),
            "-vvvv",
            "--force",
            "--entitlements", entitlements.getAbsolutePath(),
            "--options", "runtime",
            target.getAbsolutePath()
        );

        execService.exec(exec -> exec.commandLine(command));
    }

    private void createInstallerPackage(MacApplicationBundleExt config, File appFile) {
        File pkgFile = new File(config.getOutputDir(getProject()), config.getName() + ".pkg");

        List<String> command = List.of(
            "productbuild",
            "--component", appFile.getAbsolutePath(),
            "/Applications",
            "--sign", AppHelper.getEnvironmentVariable(MacApplicationBundleExt.SIGN_INSTALLER_ENV),
            pkgFile.getAbsolutePath()
        );

        execService.exec(exec -> exec.commandLine(command));
    }

    private File generateEntitlements(String sourceFile) throws IOException {
        File tempFile = File.createTempFile("entitlements-" + System.currentTimeMillis(), ".plist");

        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(sourceFile)) {
            byte[] contents = stream.readAllBytes();
            Files.write(tempFile.toPath(), contents);
        }

        return tempFile;
    }

    private void extractNativeLibraries(MacApplicationBundleExt config) throws IOException {
        File appBundle = config.locateApplicationBundle(getProject());
        File jarDir = new File(appBundle, "/Contents/Java");
        File jarFile = new File(jarDir, config.getMainJarName());
        File nativesDir = config.locateNativesDir(getProject());

        try (JarFile jar = new JarFile(jarFile)) {
            for (JarEntry entry : Collections.list(jar.entries())) {
                if (isCompatibleNativeLibrary(entry.getName(), config)) {
                    String fileName = entry.getName().substring(entry.getName().lastIndexOf("/") + 1);
                    File dylib = new File(nativesDir, fileName);
                    if (!dylib.exists()) {
                        Files.copy(jar.getInputStream(entry), dylib.toPath());
                    }
                }
            }
        }
    }

    private boolean isCompatibleNativeLibrary(String name, MacApplicationBundleExt config) {
        if (!name.endsWith(".dylib")) {
            return false;
        }

        boolean intel = name.contains("x64") || name.contains("x86");
        boolean arm = name.contains("arm64") || name.contains("aarch");
        return config.getArchitectures().contains("x86_64") ? !arm : !intel;
    }
}
