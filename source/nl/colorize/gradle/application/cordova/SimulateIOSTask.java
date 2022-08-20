//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.cordova;

import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskAction;

public class SimulateIOSTask extends DefaultTask {

    @TaskAction
    public void run() {
        ExtensionContainer ext = getProject().getExtensions();
        CordovaExt config = ext.getByType(CordovaExt.class);

        CordovaRunner runner = new CordovaRunner(getProject(), config);
        runner.run("cordova", "emulate", "ios");
    }
}
