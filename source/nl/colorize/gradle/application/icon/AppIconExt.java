//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.icon;

import nl.colorize.gradle.application.ApplicationExt;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

public interface AppIconExt extends ApplicationExt {

    @Input Property<String> getOriginal();
    @Input Property<String> getBackgroundColor();
    @Input Property<String> getOutputDir();

    @Override
    default void init(Project project) {
        getProjectDirRef().set(project.getProjectDir());
        getBuildDirRef().set(project.getLayout().getBuildDirectory().getAsFile().get());
        getBackgroundColor().convention("#000000");
        getOutputDir().convention("icons");
    }
}
