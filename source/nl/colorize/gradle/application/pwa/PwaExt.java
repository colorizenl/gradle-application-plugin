//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application.pwa;

import java.io.File;
import java.util.List;

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

    public String getPwaName() {
        return pwaName;
    }

    public void setPwaName(String pwaName) {
        this.pwaName = pwaName;
    }

    public String getPwaVersion() {
        return pwaVersion;
    }

    public void setPwaVersion(String pwaVersion) {
        this.pwaVersion = pwaVersion;
    }

    public File getWebAppDir() {
        return webAppDir;
    }

    public void setWebAppDir(File webAppDir) {
        this.webAppDir = webAppDir;
    }

    public File getServiceWorkerFile() {
        return serviceWorkerFile;
    }

    public void setServiceWorkerFile(File serviceWorkerFile) {
        this.serviceWorkerFile = serviceWorkerFile;
    }

    public File getIconFile() {
        return iconFile;
    }

    public void setIconFile(File iconFile) {
        this.iconFile = iconFile;
    }

    public List<Integer> getIconSizes() {
        return iconSizes;
    }

    public void setIconSizes(List<Integer> iconSizes) {
        this.iconSizes = iconSizes;
    }

    public File getIconOutputDir() {
        return iconOutputDir;
    }

    public void setIconOutputDir(File iconOutputDir) {
        this.iconOutputDir = iconOutputDir;
    }
}
