Gradle application plugin: build applications for Windows, Mac, iOS, Android, and the web
=========================================================================================

Gradle plugin that creates (native) applications for different platforms. It supports multiple
application types, for different source technologies and target platforms:

| Application type              | Supported source technology | Supported target platforms |
|-------------------------------|-----------------------------|----------------------------|
| Native Mac application bundle | Java                        | Mac                        |
| Native Windows MSI            | Java                        | Windows                    |
| Cordova app                   | Web app                     | iOS, Android, Mac          |
| PWA                           | Web app                     | iOS, Android, Windows      |
| Static site                   | Web app, Markdown           | Web                        |

Building mobile apps requires the development environment for the targeted platforms:

- Building iOS and/or Mac apps requires [Xcode](https://developer.apple.com/xcode/)
- Building Android apps requires the [Android SDK](https://developer.android.com/sdk/index.html)
- Building Windows installers requires [WIX Toolset](https://wixtoolset.org)

Usage
-----

The plugin is available from the [Gradle plugin registry](https://plugins.gradle.org). Adding the
plugin to the build is done by adding the following to `build.gradle`:

    plugins {
        id "nl.colorize.gradle.application" version "2023.2"
    }

Building native Mac application bundles
---------------------------------------

The plugin can create Mac OS [application bundles](https://en.wikipedia.org/wiki/Bundle_%28macOS%29).
This uses the [Ant plugin by TheInfiniteKind](https://github.com/TheInfiniteKind/appbundler), 
but makes it available via Gradle. It also adds the ability to sign the application bundle and
package it within an installer.

The Java runtime will be packaged with the application. By default, this will use the system JDK
as indicated by the `JAVA_HOME` environment variable. This can be changed to a list of custom
JDK paths by setting the `jdkPaths` configuration property. If an x86 JDK is used, generated
application bundles will support both Intel and Apple Silicon Macs. If an ARM JDK is used, only
Apple Silicon Macs will be supported.
    
The application bundle can be configured using the `macApplicationBundle` block. The configuration
options correspong to the ones from the [Ant plugin](https://github.com/TheInfiniteKind/appbundler).
The following shows an example on how to define this configuration in Gradle:

    macApplicationBundle {
        name = "Example"
        identifier = "com.example"
        description = "A description for your application"
        copyright = "Copyright 2023"
        bundleVersion = "1.0"
        icon = "resources/icon.icns"
        applicationCategory = "public.app-category.developer-tools"
        mainClassName = "com.example.app.Main"
        outputDir = "${buildDir}"
        modules = ["java.base", "java.desktop", "java.logging", "java.net.http", "jdk.crypto.ec"]
        options = ["-Xmx2g"]
        startOnFirstThread = false
    }

The following configuration options are available:

| Name                    | Required | Description                                                       |
|-------------------------|----------|-------------------------------------------------------------------|
| `name`                  | yes      | Mac application name.                                             |
| `displayName`           | no       | Optional display name, defaults to the value of `name`.           |
| `identifier`            | yes      | Apple application identfiier, in the format "com.example.name".   | 
| `bundleVersion`         | yes      | Application bundle version number.                                |
| `description`           | yes      | Short description text.                                           |
| `copyright`             | yes      | Copyright statement text.                                         |
| `applicationCategory`   | yes      | Apple application category ID.                                    |
| `minimumSystemVersion`  | no       | Minimum required Mac OS version number. Defaults to 10.13.        |
| `architectures`         | no       | List of supported CPU architectures. Default is arm64 and x86_64. |
| `mainClassName`         | yes      | Fully qualified main class name.                                  |
| `jdkPath`               | no       | Location of JDK. Defaults to `JAVA_HOME`.                         |
| `modules`               | no       | List of JDK modules.                                              |
| `options`               | no       | List of command line options.                                     |
| `startOnFirstThread`    | no       | When true, starts the application with `-XstartOnFirstThread`.    |
| `icon`                  | yes      | Location of the `.icns` file.                                     |
| `outputDir`             | no       | Output directory path, defaults to `build/mac`.                   |
    
- Note that, in addition to the `bundleVersion` property, there is also the concept of build
  version. This is normally the same as the bundle version, but can be manually specified for each
  build by setting the `buildversion` system property.
- Signing the application bundle requires an Apple Developer account and corresponding signing
  identity. The name of this identity can be set using the `MAC_SIGN_APP_IDENTITY` and
  `MAC_SIGN_INSTALLER_IDENTITY` environment variables, for signing applications and installers
  respectively.
- By default, the contents of the application will be based on all JAR files produces by the
  project, as described by the `libsDir` property. This behavior can be replaced by setting the 
  `contentDir` property in the plugin's configuration. The easiest way to bundle all content, 
  including  application binaries, resources, and libraries, is to create a single "fat JAR" file:

```
    jar.duplicatesStrategy = DuplicatesStrategy.WARN

    jar {
        from {
            configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) }
        }
     }
```
    
The plugin adds a number of tasks to the project that use this configuration:

- **createApplicationBundle**: Creates the application bundle in the specified directory.
- **signApplicationBundle**: Signs the created application bundle and packages it into an installer
  so that it can be distributed. 

Note that the tasks are *not* added to any standard tasks such as `assemble`, as Mac application
bundles can only be created when running the build on a Mac, making the tasks incompatible with
some workflows (i.e. continuous integration pipelines that tend to use Linux servers). 
    
Signing the application bundle is mandatory for distributing the application. This in turn needs
a valid Apple developer account, and corresponding certificates. You will need two certificates:
one for the application, and one for the installer. Please do not hard-code the signing identity
into the build file. It is better to define them in the Gradle properties file at
`~/.gradle/gradle.properties` and then access them from there.

Building native Windows MSI installers
--------------------------------------

Older versions of the plugin used [Launch4j](https://launch4j.sourceforge.net) to generate `.exe`
files. This has been changed to use `jpackage` to generate MSI installers, so that applications
can be distributed via the Microsoft Store. The downside is that `jpackage` requires the
[WIX Toolset](https://wixtoolset.org), which only supports Windows.

First, make sure the build produces a "far JAR", using the same instructions as described for
the native Mac app. The **packageWindows** task can then be used to create a MSI installer that
includes both the fat JAR and the Java runtime. 

The plugin can be configured using the `windows` section. The following configuration options
are available:

| Name            | Required | Description                                                     |
|-----------------|----------|-----------------------------------------------------------------|
| `inherit`       | no       | Inherits some configuration options from Mac app configuration. |
| `mainJarName`   | yes      | File name of the main JAR file. Defaults to application JAR.    |
| `mainClassName` | depends  | Fully qualified main class name.                                |
| `name`          | depends  | Windows application name.                                       |
| `version`       | depends  | Windows application version number.                             |
| `vendor`        | yes      | Vendor display name.                                            |
| `description`   | depends  | Short description text.                                         |
| `copyright`     | depends  | Copyright statement text.                                       |
| `icon`          | yes      | Location of `.ico` file.                                        |
| `uuid`          | yes      | Windows update UUID, must remain the same across versions.      |
| `outputDir`     | no       | Output directory path, defaults to `build/windows`.             |

The `inherit` can help to avoid duplicated configuration. When enabled, the `windows` configuration
will use matching configuration options defined in the `macApplicationBundle` configuration.

Building Cordova applications
-----------------------------

Use the **buildCordova** task to create [Cordova](https://cordova.apache.org) applications for 
iOS, Android, and Mac. The applications are generated from scratch, the intended usage is that the 
Cordova applications are not added to the repository, and are instead treated like build artifacts.

In addition to the development environments for iOS and Android, using this task also requires
[Cordova](https://cordova.apache.org) itself to be installed. Cordova currently *only* supports 
Java 8, it does not support newer versions. The plugin will use the environment variable 
`JAVA8_HOME` to locate the Java 8 JDK. This allows a newer Java version to be used for the build 
itself, while still supporting Cordova Java 8.

The plugin can be configured using the `cordova` block:

    cordova {
        webAppDir = "src"
        appId = "nl.colorize.test"
        appName = "Example"
        displayVersion = "1.0.0"
        icon = "icon.png"
        buildJson = "/shared/cordova-config/build.json"
    } 

The following configuration options are available:

| Name             | Required | Description                                                          |
|------------------|----------|----------------------------------------------------------------------|
| `webAppDir`      | yes      | Directory containing the web application files.                      |
| `outputDir`      | no       | Output directory for the generated apps, default is `build/cordova`. |
| `platforms`      | no       | Comma-separated list of platforms, default is `ios,android,osx`.     |
| `appId`          | yes      | Application identifier, e.g. `nl.colorize.test`.                     |
| `appName`        | yes      | Application display name.                                            |
| `displayVersion` | yes      | Application version in the format x.y.z.                             |
| `icon`           | yes      | Application icon, should be a 1024x1024 PNG image.                   |
| `buildJson`      | yes      | Location of the Cordova `build.json` configuration file.             |
| `dist`           | no       | Build distribution type, either 'release' (default) or 'debug'.      |

Note that, in addition to the `displayVersion` property, there is also the concept of build
version. This is normally the same as the display version, but can be manually specified for each
build by setting the `buildversion` system property. 

In addition to the actual build, the plugin also adds two convenience tasks, **simulateIOS** and
**simulateAndroid**, to start an iOS/Android simulator for the generated Cordova apps.

Building PWAs
-------------

The plugin can also repackage web applications into
[Progressive Web Applications](https://en.wikipedia.org/wiki/Progressive_web_application).
The PWA can then in turn be repackaged into native applications using services like
[PWA Builder](https://www.pwabuilder.com). 

The **generatePWA** task creates a PWA from an existing web application. It will insert the
[web app manifest](https://developer.mozilla.org/en-US/docs/Web/Manifest) into the HTML, and also
insert HTML that registers a
[service worker](https://developer.mozilla.org/en-US/docs/Web/API/Service_Worker_API).

These tasks share the same configuration via the `pwa` configuration section. The following 
configuration options are available:

| Name            | Required | Description                                                   |
|-----------------|----------|---------------------------------------------------------------|
| `webAppDir`     | yes      | Input directory where the original web app is located.        |
| `outputDir`     | no       | Output directory for the generated PWA.                       |
| `manifest`      | yes      | Location of the web app manifest JSON file.                   |
| `serviceWorker` | no       | Location of service worker file, will use default if omitted. |
| `cacheName`     | yes      | Name of the cache the service worker will use.                |

Building a static site
----------------------

The plugin can take content written in Markdown or plain HTML, and then render this content using
templates to create a [static site](https://en.wikipedia.org/wiki/Static_web_page). This is done
using the **generateStaticSite** task, which uses the following logic:

- The output directory structure will match the content directory structure.
- Each directory can include a file called `template.html`. 
- The template contains a tag `<clrz-content>`, the contents of this tag will be replaced with
  the article content.
- For HTML files, the file content is used as the article.
- For Markdown files, the file is first rendered to HTML, which is then used as the article.
- Subdirectories can contain their own `template.html` file. If so, the process described above
  is repeated recursively.
- All files except HTML and Markdown are retained in the static site. 

The static site is configured using the `staticSite` configuration section. The following options
are available:

| Name               | Required | Default       | Description                                       |
|--------------------|----------|---------------|---------------------------------------------------|
| `contentDir`       | no       | content       | Content directory, relative to project directory. |
| `outputDir`        | no       | staticsite    | Output directory, relative to build directory.    |
| `templateFileName` | no       | template.html | File name used as template instead of content.    |

The **serveStaticSite** task can be used to test the generated static site using a local HTTP server,
at [http://localhost:7777](http://localhost:7777).

Instructions for building the plugin itself
-------------------------------------------

Building the plugin itself can only be done on Mac OS. It also requires the following:

- [Java JDK](http://java.oracle.com) 17+
- [Gradle](http://gradle.org)
- [Ant](https://ant.apache.org)

The following Gradle build tasks are available:

- `gradle clean` cleans the build directory
- `gradle assemble` creates the JAR file for distribution
- `gradle test` runs all unit tests
- `gradle coverage` runs all unit tests and reports on test coverage
- `gradle publishPlugins` publishes the plugin to the Gradle plugin portal (requires account)

Some tests can only run on Windows or Mac. Add `-Pheadless=true` when running in a headless
environment to exclude those tests.

**Note:** Running the tests currently requires some additional JVM properties on Java 17+,
due to a [Gradle issue](https://github.com/gradle/gradle/issues/18647). These properties are
automatically used when running from Gradle, but need to be added manually when running tests 
from an IDE.

Testing the plugin using an example application
-----------------------------------------------

The plugin comes with an example application, that can be used to test the plugin on itself:

- First run `gradle assemble` to build the plugin itself.
- Navigate to the `example` directory to build the example app.
  - Run `gradle createApplicationBundle` to create a Mac application bundle.
  - Run `gradle signApplicationBundle` to sign a Mac application bundle.
  - Run `gradle packageMSI` to create a Windows MSI installer.
  - Run `gradle generateStaticSite` to generate a website from Markdown templates.
  - Run `gradle generatePWA` to create a PWA version of the aforementioned website.

Note signing the Mac application requires environment variables for the signing identity. See
the documentation section on the Mac-specific configurtion for details. 
  
License
-------

Copyright 2010-2023 Colorize

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
