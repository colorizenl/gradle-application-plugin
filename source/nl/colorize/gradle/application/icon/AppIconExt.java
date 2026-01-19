//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.icon;

import lombok.Getter;
import lombok.Setter;
import nl.colorize.gradle.application.AppHelper;
import nl.colorize.gradle.application.Validatable;

@Getter
@Setter
public class AppIconExt implements Validatable {

    private String original;
    private String outputDir;

    public AppIconExt() {
        this.outputDir = "icons";
    }

    @Override
    public void validate() {
        AppHelper.check(original != null, "Missing appIcon.original");
    }
}
