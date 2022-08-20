//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.cordova;

import org.gradle.api.Project;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class CordovaRunner {

    private Project project;
    private CordovaExt config;

    public CordovaRunner(Project project, CordovaExt config) {
        this.project = project;
        this.config = config;
    }

    public void run(String... command) {
        run(Arrays.asList(command));
    }

    public void run(List<String> command) {
        File outputDir = config.prepareOutputDir(project);
        String java8Home = System.getenv("JAVA8_HOME");

        if (java8Home == null) {
            throw new IllegalStateException("Cordova requires environment variable JAVA8_HOME " +
                "that refers to Java 8");
        }

        // This intentionally uses stdout and not the Gradle plugin
        // logging API. We want to explicitly show the Cordova
        // commands, as this helps a lot to determine the cause of
        // configuration errors.
        System.out.println(String.join(" ", command));

        project.exec(exec -> {
            exec.commandLine(command);
            exec.environment("JAVA_HOME", java8Home);
            exec.workingDir(outputDir.getAbsolutePath());
        });
    }
}
