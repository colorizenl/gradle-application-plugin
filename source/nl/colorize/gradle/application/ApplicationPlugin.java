//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application;

import nl.colorize.gradle.application.cordova.BuildCordovaTask;
import nl.colorize.gradle.application.cordova.CordovaExt;
import nl.colorize.gradle.application.cordova.SimulateAndroidTask;
import nl.colorize.gradle.application.cordova.SimulateIOSTask;
import nl.colorize.gradle.application.macapplicationbundle.CreateApplicationBundleTask;
import nl.colorize.gradle.application.macapplicationbundle.MacApplicationBundleExt;
import nl.colorize.gradle.application.macapplicationbundle.SignApplicationBundleTask;
import nl.colorize.gradle.application.pwa.GeneratePwaTask;
import nl.colorize.gradle.application.pwa.PwaExt;
import nl.colorize.gradle.application.staticsite.GenerateStaticSiteTask;
import nl.colorize.gradle.application.staticsite.ServeStaticSiteTask;
import nl.colorize.gradle.application.staticsite.StaticSiteExt;
import nl.colorize.gradle.application.windows.PackageMSITask;
import nl.colorize.gradle.application.windows.WindowsExt;
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
        configureCordova(project);
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
        ext.create("windows", WindowsExt.class);

        TaskContainer tasks = project.getTasks();
        tasks.create("packageMSI", PackageMSITask.class);

        tasks.getByName("packageMSI").dependsOn("jar");
    }

    private void configureCordova(Project project) {
        ExtensionContainer ext = project.getExtensions();
        ext.create("cordova", CordovaExt.class);

        TaskContainer tasks = project.getTasks();
        tasks.create("buildCordova", BuildCordovaTask.class);
        tasks.create("simulateIOS", SimulateIOSTask.class);
        tasks.create("simulateAndroid", SimulateAndroidTask.class);
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
