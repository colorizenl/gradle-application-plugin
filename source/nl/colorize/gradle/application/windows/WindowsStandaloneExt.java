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

import java.io.File;
import java.util.Collections;

public interface WindowsStandaloneExt extends ApplicationExt {

    @Input Property<String> getMainJarName();
    @Input ListProperty<String> getArgs();
    @Input Property<String> getName();
    @Input Property<String> getVersion();
    @Input Property<String> getIcon();
    @Input Property<String> getSupportURL();
    @Input Property<Integer> getMemory();
    @Input Property<String> getExeFileName();
    @Input Property<String> getJavaVersion();

    @Override
    default void init(Project project) {
        getProjectDirRef().set(project.getProjectDir());
        getBuildDirRef().set(project.getLayout().getBuildDirectory().getAsFile().get());

        getArgs().convention(Collections.emptyList());
        getMemory().convention(2048);
        getJavaVersion().convention("25");
        getExeFileName().convention("");

        var macConfig = project.getExtensions().getByType(MacApplicationBundleExt.class);
        getName().convention(macConfig.getName());
        getVersion().convention(macConfig.getBundleVersion());
        getMainJarName().convention(macConfig.getMainJarName());
    }

    default File locateExeFile() {
        String fileName = getExeFileName().get();
        if (fileName.isEmpty()) {
            fileName = getMainJarName().get().replace(".jar", ".exe");
        }
        return toOutputFile(fileName);
    }
}
