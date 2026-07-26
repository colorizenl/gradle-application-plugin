//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.staticsite;

import nl.colorize.gradle.application.ApplicationExt;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

public interface StaticSiteExt extends ApplicationExt {

    @Input Property<String> getContentDir();
    @Input Property<String> getOutputDir();
    @Input Property<String> getTemplateFileName();

    @Override
    default void init(Project project) {
        getProjectDirRef().set(project.getProjectDir());
        getBuildDirRef().set(project.getLayout().getBuildDirectory().getAsFile().get());
        getContentDir().convention("content");
        getOutputDir().convention("staticsite");
        getTemplateFileName().convention("template.html");
    }
}
