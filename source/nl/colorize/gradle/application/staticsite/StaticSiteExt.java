//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.staticsite;

import lombok.Data;

import java.util.List;

@Data
public class StaticSiteExt {

    private String contentDir;
    private String outputDir;
    private String templateFileName;

    public static final List<String> TEMPLATE_TAGS = List.of("clrz-content", "colorize-content");

    public StaticSiteExt() {
        this.contentDir = "content";
        this.outputDir = "staticsite";
        this.templateFileName = "template.html";
    }
}
