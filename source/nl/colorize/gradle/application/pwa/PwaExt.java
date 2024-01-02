//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.pwa;

import lombok.Data;
import nl.colorize.gradle.application.AppHelper;
import nl.colorize.gradle.application.Validatable;
import org.gradle.api.Project;

import java.io.File;

@Data
public class PwaExt implements Validatable {

    private String webAppDir;
    private String outputDir;
    private String manifest;
    private String serviceWorker;
    private String cacheName;

    public PwaExt() {
        this.outputDir = "pwa";
    }

    public File getOutputDir(Project project) {
        return AppHelper.getOutputDir(project, outputDir);
    }

    @Override
    public void validate() {
        AppHelper.check(webAppDir != null, "Missing pwa.webAppDir");
        AppHelper.check(manifest != null, "Missing pwa.manifest");
        AppHelper.check(cacheName != null, "Missing pwa.cacheName");

        File indexFile = new File(webAppDir, "index.html");
        AppHelper.check(indexFile.exists(), "pwa.webAppDir not contain index.html");
    }
}
