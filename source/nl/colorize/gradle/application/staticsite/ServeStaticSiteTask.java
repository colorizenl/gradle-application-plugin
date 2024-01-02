//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.staticsite;

import fi.iki.elonen.SimpleWebServer;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

public class ServeStaticSiteTask extends DefaultTask {

    @TaskAction
    public void run() {
        StaticSiteExt config = getProject().getExtensions().getByType(StaticSiteExt.class);
        File outputDir = config.getOutputDir(getProject());
        int port = config.getLocalServerPort();

        try {
            SimpleWebServer httpServer = new SimpleWebServer("localhost", port, outputDir, true);
            httpServer.start();
            Thread.sleep(Long.MAX_VALUE);
        } catch (IOException | InterruptedException e) {
            // Terminate HTTP server.
        }
    }
}
