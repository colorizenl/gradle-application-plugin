//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.staticsite;

import lombok.Data;
import nl.colorize.gradle.application.AppHelper;
import nl.colorize.gradle.application.Validatable;
import org.gradle.api.Project;

import java.io.File;
import java.util.List;

@Data
public class StaticSiteExt implements Validatable {

    private String contentDir;
    private String outputDir;
    private String templateFileName;
    private int localServerPort;

    public static final List<String> TEMPLATE_TAGS = List.of(
        "clrz-content",
        "colorize-content"
    );

    public StaticSiteExt() {
        this.contentDir = "content";
        this.outputDir = "staticsite";
        this.templateFileName = "template.html";
        this.localServerPort = 7777;
    }

    public File getOutputDir(Project project) {
        return AppHelper.getOutputDir(project, outputDir);
    }

    @Override
    public void validate() {
        // All properties are optional.
    }
}
