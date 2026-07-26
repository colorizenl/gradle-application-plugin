//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.xcode;

import nl.colorize.gradle.application.ApplicationExt;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

public interface XcodeGenExt extends ApplicationExt {

    @Input Property<String> getAppId();
    @Input Property<String> getBundleId();
    @Input Property<String> getAppName();
    @Input Property<String> getBundleVersion();
    @Input Property<String> getIcon();
    @Input Property<String> getIconBackgroundColor();
    @Input Property<String> getDeploymentTarget();
    @Input Property<String> getResourcesDir();
    @Input Property<String> getLaunchScreenColor();
    @Input Property<String> getOutputDir();
    @Input Property<String> getXcodeGenPath();

    @Override
    default void init(Project project) {
        getProjectDirRef().set(project.getProjectDir());
        getBuildDirRef().set(project.getLayout().getBuildDirectory().getAsFile().get());
        getIconBackgroundColor().convention("#000000");
        getOutputDir().convention("xcode");
        getDeploymentTarget().convention("14.0");
        getLaunchScreenColor().convention("#000000");
        getXcodeGenPath().convention("/usr/local/bin/xcodegen");
    }
}
