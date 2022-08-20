//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.pwa;

import lombok.Data;

import java.io.File;
import java.util.List;

@Data
public class PwaExt {

    private String pwaName;
    private String pwaVersion;
    private File webAppDir;
    private File serviceWorkerFile;

    private File iconFile;
    private List<Integer> iconSizes;
    private File iconOutputDir;

    public PwaExt() {
        this.pwaVersion = "1.0";
        this.iconSizes = List.of(48, 72, 96, 144, 168, 192);
    }
}
