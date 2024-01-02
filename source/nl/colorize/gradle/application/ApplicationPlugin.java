//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application;

import nl.colorize.gradle.application.macapplicationbundle.CreateApplicationBundleTask;
import nl.colorize.gradle.application.macapplicationbundle.MacApplicationBundleExt;
import nl.colorize.gradle.application.macapplicationbundle.SignApplicationBundleTask;
import nl.colorize.gradle.application.pwa.GeneratePwaTask;
import nl.colorize.gradle.application.pwa.PwaExt;
import nl.colorize.gradle.application.staticsite.GenerateStaticSiteTask;
import nl.colorize.gradle.application.staticsite.ServeStaticSiteTask;
import nl.colorize.gradle.application.staticsite.StaticSiteExt;
import nl.colorize.gradle.application.windowsexe.PackageWindowsStandaloneTask;
import nl.colorize.gradle.application.windowsexe.WindowsStandaloneExt;
import nl.colorize.gradle.application.windowsmsi.PackageMSITask;
import nl.colorize.gradle.application.windowsmsi.WindowsInstallerExt;
import nl.colorize.gradle.application.xcode.XcodeGenExt;
import nl.colorize.gradle.application.xcode.XcodeGenTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
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
    }

    private void configureMacApplicationBundle(Project project) {
        ExtensionContainer ext = project.getExtensions();
        ext.create("macApplicationBundle", MacApplicationBundleExt.class);

        TaskContainer tasks = project.getTasks();
        tasks.create("createApplicationBundle", CreateApplicationBundleTask.class);
        tasks.create("signApplicationBundle", SignApplicationBundleTask.class);

        tasks.getByName("signApplicationBundle").dependsOn(tasks.getByName("createApplicationBundle"));
        tasks.getByName("createApplicationBundle").dependsOn("jar");
    }

    private void configureWindows(Project project) {
        ExtensionContainer ext = project.getExtensions();
        ext.create("msi", WindowsInstallerExt.class);
        ext.create("exe", WindowsStandaloneExt.class);

        TaskContainer tasks = project.getTasks();
        tasks.create("packageMSI", PackageMSITask.class);
        tasks.create("packageEXE", PackageWindowsStandaloneTask.class);

        tasks.getByName("packageMSI").dependsOn("jar");
        tasks.getByName("packageEXE").dependsOn("jar");
    }

    private void configureXcodeGen(Project project) {
        ExtensionContainer ext = project.getExtensions();
        ext.create("xcode", XcodeGenExt.class);

        TaskContainer tasks = project.getTasks();
        tasks.create("xcodeGen", XcodeGenTask.class);
    }

    private void configurePWA(Project project) {
        ExtensionContainer ext = project.getExtensions();
        ext.create("pwa", PwaExt.class);

        TaskContainer tasks = project.getTasks();
        tasks.create("generatePWA", GeneratePwaTask.class);
    }

    private void configureStaticSite(Project project) {
        ExtensionContainer ext = project.getExtensions();
        ext.create("staticSite", StaticSiteExt.class);

        TaskContainer tasks = project.getTasks();
        tasks.create("generateStaticSite", GenerateStaticSiteTask.class);
        tasks.create("serveStaticSite", ServeStaticSiteTask.class);

        tasks.getByName("serveStaticSite").dependsOn(tasks.getByName("generateStaticSite"));
    }
}
