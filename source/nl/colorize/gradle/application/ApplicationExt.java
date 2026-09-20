//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

import java.io.File;

/**
 * Shared parent interface for all configuration extensions used by the
 * application plugin. The convention is oneconfiguration extension per
 * "sub-plugin". If there are multiple tasks within the same sub-plugin,
 * they will share the same configuration extension.
 */
public interface ApplicationExt {

    @Input Property<File> getProjectDirRef();
    @Input Property<File> getBuildDirRef();

    /**
     * Initializes this configuration extension. This included defining
     * default values for all optional properties. This method is called
     * by the plugin during the configuration phase.
     */
    public void init(Project project);

    default File toProjectFile(String path) {
        if (path.startsWith("/")) {
            return new File(path);
        } else {
            File projectDir = getProjectDirRef().get();
            return new File(projectDir, path);
        }
    }

    default File toOutputFile(String path) {
        File buildDir = getBuildDirRef().get();
        AppHelper.mkdir(buildDir);
        return new File(buildDir, path);
    }

    default File toOutputDir(String path) {
        File buildDir = getBuildDirRef().get();
        AppHelper.mkdir(buildDir);
        File outputDir = new File(buildDir, path);
        AppHelper.mkdir(outputDir);;
        return outputDir;
    }

    default File locateLibsDir() {
        // Gradle 7 and higher no longer have the libsDir property.
        return toOutputDir("libs");
    }
}
