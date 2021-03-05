//-----------------------------------------------------------------------------
// Gradle Mac Application Bundle Plugin
// Copyright 2010-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.macapplicationbundle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskContainer;

public class ApplicationBundlePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        ExtensionContainer ext = project.getExtensions();
        ext.create("macApplicationBundle", MacApplicationBundleExt.class);

        TaskContainer tasks = project.getTasks();
        tasks.create("createApplicationBundle", CreateApplicationBundleTask.class);
        tasks.create("signApplicationBundle", SignApplicationBundleTask.class);
        tasks.create("createICNS", CreateICNSTask.class);

        tasks.getByName("signApplicationBundle").dependsOn(tasks.getByName("createApplicationBundle"));
        tasks.getByName("createApplicationBundle").dependsOn("jar");
    }
}
