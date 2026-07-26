//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application;

import nl.colorize.gradle.application.icon.AppIconExt;
import nl.colorize.gradle.application.icon.GenerateAppIconsTask;
import nl.colorize.gradle.application.macapplicationbundle.CreateApplicationBundleTask;
import nl.colorize.gradle.application.macapplicationbundle.MacApplicationBundleExt;
import nl.colorize.gradle.application.macapplicationbundle.PackageApplicationBundleTask;
import nl.colorize.gradle.application.macapplicationbundle.SignApplicationBundleTask;
import nl.colorize.gradle.application.pwa.GeneratePwaTask;
import nl.colorize.gradle.application.pwa.PwaExt;
import nl.colorize.gradle.application.staticsite.GenerateStaticSiteTask;
import nl.colorize.gradle.application.staticsite.StaticSiteExt;
import nl.colorize.gradle.application.windows.PackageMSITask;
import nl.colorize.gradle.application.windows.PackageWindowsExeTask;
import nl.colorize.gradle.application.windows.WindowsInstallerExt;
import nl.colorize.gradle.application.windows.WindowsStandaloneExt;
import nl.colorize.gradle.application.xcode.XcodeGenExt;
import nl.colorize.gradle.application.xcode.XcodeGenTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.tasks.TaskContainer;

/**
 * Main entry poin for the plugin. This is essentially different plugins rolled
 * into one, so it will create the configuration and tasks for each of them.
 */
public class ApplicationPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        configureMacApplicationBundle(project);
        configureWindows(project);
        configureXcodeGen(project);
        configurePWA(project);
        configureStaticSite(project);
        configureAppIcon(project);
    }

    private void configureMacApplicationBundle(Project project) {
        var ext = createExt(project, "macApplicationBundle", MacApplicationBundleExt.class);

        TaskContainer tasks = project.getTasks();
        tasks.register("createApplicationBundle", CreateApplicationBundleTask.class,
            task -> task.getExt().set(ext));
        tasks.register("signApplicationBundle", SignApplicationBundleTask.class,
            task -> task.getExt().set(ext));
        tasks.register("packageApplicationBundle", PackageApplicationBundleTask.class,
            task -> task.getExt().set(ext));

        tasks.getByName("signApplicationBundle").dependsOn(tasks.getByName("createApplicationBundle"));
        tasks.getByName("createApplicationBundle").dependsOn("jar");
        tasks.getByName("packageApplicationBundle").dependsOn("jar");
        if (hasShadowJarPlugin(project)) {
            tasks.getByName("createApplicationBundle").dependsOn("shadowJar");
            tasks.getByName("packageApplicationBundle").dependsOn("shadowJar");
        }
    }

    private void configureWindows(Project project) {
        WindowsInstallerExt msiExt = createExt(project, "msi", WindowsInstallerExt.class);
        WindowsStandaloneExt exeExt = createExt(project, "exe", WindowsStandaloneExt.class);

        TaskContainer tasks = project.getTasks();
        tasks.register("packageMSI", PackageMSITask.class, task -> task.getExt().set(msiExt));
        tasks.register("packageEXE", PackageWindowsExeTask.class, task -> task.getExt().set(exeExt));

        tasks.getByName("packageMSI").dependsOn("jar");
        tasks.getByName("packageEXE").dependsOn("jar");
        if (hasShadowJarPlugin(project)) {
            tasks.getByName("packageMSI").dependsOn("shadowJar");
            tasks.getByName("packageEXE").dependsOn("shadowJar");
        }
    }

    private void configureXcodeGen(Project project) {
        XcodeGenExt ext = createExt(project, "xcode", XcodeGenExt.class);

        project.getTasks().register(
            "xcodeGen",
            XcodeGenTask.class,
            task -> task.getExt().set(ext)
        );
    }

    private void configurePWA(Project project) {
        PwaExt ext = createExt(project, "pwa", PwaExt.class);

        project.getTasks().register(
            "generatePWA",
            GeneratePwaTask.class,
            task -> task.getExt().set(ext)
        );
    }

    private void configureStaticSite(Project project) {
        StaticSiteExt ext = createExt(project, "staticSite", StaticSiteExt.class);

        project.getTasks().register(
            "generateStaticSite",
            GenerateStaticSiteTask.class,
            task -> task.getExt().set(ext)
        );
    }

    private void configureAppIcon(Project project) {
        AppIconExt ext = createExt(project, "appIcon", AppIconExt.class);

        project.getTasks().register(
            "generateAppIcons",
            GenerateAppIconsTask.class,
            task -> task.getExt().set(ext)
        );
    }

    private <T extends ApplicationExt> T createExt(Project project, String name, Class<T> type) {
        T ext = project.getExtensions().create(name, type);
        ext.init(project);
        return ext;
    }

    private boolean hasShadowJarPlugin(Project project) {
        try {
            project.getTasks().getByName("shadowJar");
            return true;
        } catch (UnknownTaskException e) {
            return false;
        }
    }
}
