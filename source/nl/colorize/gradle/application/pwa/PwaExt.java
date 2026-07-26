//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.pwa;

import nl.colorize.gradle.application.ApplicationExt;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

public interface PwaExt extends ApplicationExt {

    @Input Property<String> getWebAppDir();
    @Input Property<String> getOutputDir();
    @Input Property<String> getManifest();
    @Input Property<String> getServiceWorker();
    @Input Property<String> getCacheName();

    @Override
    default void init(Project project) {
        getProjectDirRef().set(project.getProjectDir());
        getBuildDirRef().set(project.getLayout().getBuildDirectory().getAsFile().get());
        getOutputDir().convention("pwa");
        getServiceWorker().convention("");
    }
}
