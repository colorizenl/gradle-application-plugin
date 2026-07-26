//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.windows;

import nl.colorize.gradle.application.ApplicationExt;
import nl.colorize.gradle.application.macapplicationbundle.MacApplicationBundleExt;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

import java.util.Collections;

public interface WindowsInstallerExt extends ApplicationExt {

    @Input Property<String> getMainJarName();
    @Input Property<String> getMainClassName();
    @Input ListProperty<String> getOptions();
    @Input ListProperty<String> getArgs();
    @Input Property<String> getName();
    @Input Property<String> getVersion();
    @Input Property<String> getVendor();
    @Input Property<String> getDescription();
    @Input Property<String> getCopyright();
    @Input Property<String> getIcon();
    @Input Property<String> getUuid();
    @Input Property<String> getOutputDir();

    @Override
    default void init(Project project) {
        getProjectDirRef().set(project.getProjectDir());
        getBuildDirRef().set(project.getLayout().getBuildDirectory().getAsFile().get());

        getOptions().convention(Collections.emptyList());
        getArgs().convention(Collections.emptyList());
        getOutputDir().convention("windows-msi");

        var macConfig = project.getExtensions().getByType(MacApplicationBundleExt.class);
        getMainJarName().convention(macConfig.getMainJarName());
        getMainClassName().convention(macConfig.getMainClassName());
        getName().convention(macConfig.getName());
        getVersion().convention(macConfig.getBundleVersion());
        getDescription().convention(macConfig.getDescription());
        getCopyright().convention(macConfig.getCopyright());
    }
}
