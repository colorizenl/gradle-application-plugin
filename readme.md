Gradle application plugin: Build applications for Windows, Mac, iOS, Android, and web
=====================================================================================

[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/nl.colorize.gradle.application)](
https://plugins.gradle.org/plugin/nl.colorize.gradle.application)
[![License](https://img.shields.io/badge/license-apache_2.0-purple)](
https://www.apache.org/licenses/LICENSE-2.0)

Gradle plugin that builds native or hybrid applications for different platforms. It supports
multiple application types for different source technologies and target platforms:

| Application type              | Source technology | Target platforms      |
|-------------------------------|-------------------|-----------------------|
| Native Mac application bundle | Java              | Mac                   |
| Native Windows MSI            | Java              | Windows               |
| Native Windows EXE            | Java              | Windows               |
| Hybrid iOS app Xcode project  | Swift, web app    | iOS                   |
| PWA                           | Web app           | iOS, Android, Windows |
| Static site                   | Web app, Markdown | Web                   |

This allows you to distribute the same application to multiple platforms, without having to
maintain platform-specific code.

System requirements
-------------------

This plugin is basically a Gradle plugin interface on top of the native toolchain for these
platforms. That unfortunately means the plugin requires the native toolchain for each platform
to be available in the build environment:

| Target platform | Build platform(s)   | Requires                                                    |
|-----------------|---------------------|-------------------------------------------------------------|
| Windows MSI     | Windows             | [WIX Toolset](https://wixtoolset.org)                       |
| Windows EXE     | Windows, Mac, Linux | [Launch4j](https://launch4j.sourceforge.net)                |
| Mac             | Mac                 | Xcode                                                       |
| iOS             | Mac                 | Xcode, [XcodeGen](https://github.com/yonaskolb/XcodeGen)    |
| Android         | Windows, Mac, Linux | Android SDK                                                 |

Usage
-----

The plugin is available from the [Gradle plugin registry](https://plugins.gradle.org). You can
use the plugin in your Gradle project by adding the following to `build.gradle`:

    plugins {
        id "nl.colorize.gradle.application" version "2025.3"
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
Apple Silicon Macs will be supported. The embedded JDK should not be in quarantine, i.e. the
`com.apple.quarantine` attribute should not be present on any of the files within the JDK.

The following OpenJDK distributions are supported as embedded JDK in the application bundle:

- [Eclipse Temurin](https://adoptium.net/temurin/releases)
- [Azul Zulu](https://www.azul.com/downloads/#zulu)
- [Amazon Corretto](https://aws.amazon.com/corretto/)
    
The application bundle can be configured using the `macApplicationBundle` block. The configuration
options correspong to the ones from the [Ant plugin](https://github.com/TheInfiniteKind/appbundler).
The following shows an example on how to define this configuration in Gradle:

    macApplicationBundle {
        name = "Example"
        identifier = "com.example"
        description = "A description for your application"
        copyright = "Copyright 2025"
        bundleVersion = "1.0"
        icon = "resources/icon.icns"
        applicationCategory = "public.app-category.developer-tools"
        mainClassName = "com.example.app.Main"
        outputDir = "${buildDir}"
        options = ["-Xmx2g"]
    }

The following configuration options are available:

| Name                   | Required | Description                                                      |
|------------------------|----------|------------------------------------------------------------------|
| `name`                 | yes      | Mac application name.                                            |
| `displayName`          | no       | Optional display name, defaults to the value of `name`.          |
| `identifier`           | yes      | Apple application identfiier, in the format "com.example.name".  | 
| `bundleVersion`        | yes      | Application bundle version number.                               |
| `description`          | yes      | Short description text.                                          |
| `copyright`            | yes      | Copyright statement text.                                        |
| `applicationCategory`  | yes      | Apple application category ID.                                   |
| `minimumSystemVersion` | no       | Minimum required Mac OS version number. Defaults to 10.13.       |
| `architectures`        | no       | Supported CPU architectures. Default is [`arm64`, `x86_64`].     |
| `mainJarName`          | yes      | File name for the JAR file containing the main class.            |
| `mainClassName`        | yes      | Fully qualified main class name.                                 |
| `jdkPath`              | no       | Location of JDK. Defaults to `JAVA_HOME`.                        |
| `modules`              | no       | Overrides list of embedded JDK modules.                          |
| `additionalModules`    | no       | Extends default list of embedded JDK modules.                    | 
| `options`              | no       | List of JVM command line options.                                |
| `args`                 | no       | List of command line arguments provided to the main class.       |
| `icon`                 | yes      | Location of the `.icns` file.                                    |
| `signNativeLibraries`  | no       | Signs native libraries embedded in the application's JAR files.  |
| `additionalBinaries`   | no       | List of files that should be embedded in the application bundle. | 
| `outputDir`            | no       | Output directory path, defaults to `build/mac`.                  |

The application bundle includes a Java runtime. This does not include the full JDK, to reduce
the bundle size. The list of JDK modules can be extended using the `additionalModules` property,
or replaced entirely using the `modules` property. By default, the following JDK modules are
included in the runtime:

- `java.base`
- `java.desktop`
- `java.logging`
- `java.net.http`
- `java.sql`
- `jdk.crypto.ec`

Mac applications use two different version numbers: The application version and the build version.
By default, both are based on the `bundleVersion` property. It is possible to specify the build
version on the command line (it's not a property since the build version is supposed to be unique
for every build). The build version can be set using the `buildversion` system property, e.g.
`gradle -Dbuildversion=1.0.1 createApplicationBundle`.

Signing the application bundle requires an Apple Developer account and corresponding signing
identity. The name of this identity can be set using the `MAC_SIGN_APP_IDENTITY` and
`MAC_SIGN_INSTALLER_IDENTITY` environment variables, for signing applications and installers
respectively.

You will need to provide a "fat JAR" that defines a main class, and contains both your application
and all of its dependencies. The following example shows how to turn your project's default JAR
file into a fat JAR:

    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        exclude "**/module-info.class"
        exclude "**/META-INF/INDEX.LIST"
        exclude "**/META-INF/*.SF"
        exclude "**/META-INF/*.DSA"
        exclude "**/META-INF/*.RSA"
        
        manifest {
            attributes "Main-Class": "com.example.ExampleApp"
        }
    
        from {
            configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) }
        }
    }

There are alternative ways to create a fat JAR, if you need to retain the project's "normal"
JAR file: You can [create a Gradle task](https://stackoverflow.com/a/61198352/79505) that will
create the fat JAR in in addition to the normal JAR. You can also use the
[Gradle shadow JAR plugin](https://github.com/Goooler/shadow) to achieve the same effect.
    
The plugin adds a number of tasks to the project that use this configuration:

- **createApplicationBundle**: Creates the application bundle in the specified directory.
- **signApplicationBundle**: Signs the created application bundle and packages it into an
  installer so that it can be distributed. 
- **packageApplicationBundle**: An *experimental* task that creates the application bundle using
  the [jpackage](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html) tool
  that is included with the JDK. Creates both a DMG file and a PKG installer. This task is
  experimental, it does not yet support all options from the *createApplicationBundle* and
  *signApplicationBundle* tasks.

Note that the tasks are *not* added to any standard tasks such as `assemble`, as Mac application
bundles can only be created when running the build on a Mac, making the tasks incompatible with
some workflows (i.e. continuous integration pipelines that tend to use Linux servers). 
    
Signing the application bundle is mandatory for distributing the application. This in turn needs
a valid Apple developer account, and corresponding certificates. You will need two certificates:
one for the application, and one for the installer. Please do not hard-code the signing identity
into the build file. It is better to define them in `~/.gradle/gradle.properties` or via 
environment variables.

Building native Windows MSI installers
--------------------------------------

Uses `jpackage` to generate MSI installers, so that applications can be distributed via the
Microsoft Store. The downside is that using `jpackage` requires the
[WIX Toolset](https://wixtoolset.org), which only supports Windows.

First, make sure the build produces a "fat JAR", using the same instructions as described for
the native Mac app. The **packageMSI** task can then be used to create a MSI installer that
includes both the fat JAR and the Java runtime. 

The plugin can be configured using the `msi` section. The following configuration options
are available:

| Name            | Required | Description                                                     |
|-----------------|----------|-----------------------------------------------------------------|
| `inherit`       | no       | Inherits some configuration options from Mac app configuration. |
| `mainJarName`   | depends  | File name of the main JAR file. Defaults to application JAR.    |
| `mainClassName` | depends  | Fully qualified main class name.                                |
| `options`       | no       | List of JVM command line options.                               |
| `args`          | no       | List of command line arguments provided to the main class.      |
| `name`          | depends  | Windows application name.                                       |
| `version`       | depends  | Windows application version number.                             |
| `vendor`        | yes      | Vendor display name.                                            |
| `description`   | depends  | Short description text.                                         |
| `copyright`     | depends  | Copyright statement text.                                       |
| `icon`          | yes      | Location of `.ico` file.                                        |
| `uuid`          | yes      | Windows update UUID, must remain the same across versions.      |
| `outputDir`     | no       | Output directory path, defaults to `build/windows-msi`.         |

The `inherit` option can help to avoid duplicated configuration. When enabled, the `msi`
configuration will use matching configuration options defined in the `macApplicationBundle`
configuration.

Building a native Windows EXE 
-----------------------------

Creates a native Windows `.exe` file, then packages that file with both the application and an
embedded Java runtime to create a Windows application. This is an alternative for creating Windows
MSI installers, which is less portable but has the benefit that it supports non-Windows build
platforms. The `.exe` file is created using [Launch4j](https://launch4j.sourceforge.net).

The **packageEXE** task will create both the `.exe` file and the Windows application. It can be
configured using the `exe` section:

| Name          | Required | Description                                                     |
|---------------|----------|-----------------------------------------------------------------|
| `inherit`     | no       | Inherits some configuration options from Mac app configuration. |
| `mainJarName` | depends  | File name of the main JAR file. Defaults to application JAR.    |
| `args`        | no       | List of command line arguments provided to the main class.      |
| `name`        | depends  | Windows application name.                                       |
| `version`     | depends  | Windows application version number.                             |
| `icon`        | yes      | Location of `.ico` file.                                        |
| `supportURL`  | yes      | Shown in case of application launch errors.                     |
| `memory`      | no       | Maximum application memory in megabytes. Defaults to 2048 MB.   |
| `exeFileName` | no       | File name for `.exe` file. Based on JAR file name if omitted.   |

The `inherit` option can help to avoid duplicated configuration. When enabled, the `exe`
configuration will use matching configuration options defined in the `macApplicationBundle`
configuration.

The Windows application contain an embedded Java runtime, which is located using the 
`EMBEDDED_WINDOWS_JAVA` environment variable. This directory needs to contain a subdirectory
named `java`, which will be embedded in the Windows application. The configuration intentionally
does *not* use `JAVA_HOME`, so that the Windows application can also be created on non-Windows
build platforms.

The location of Launch4j can be specified using the `LAUNCH4J_HOME` environment variable.

Generating Xcode projects
-------------------------

The plugin can generate [Xcode](https://en.wikipedia.org/wiki/Xcode) projects for hybrid iOS apps,
that consist of a Swift app with a web view, that in turn displays the original web application.
The web application files are packaged within the app, they are not loaded from an external
server. This approach is similar to [Cordova](https://cordova.apache.org), but is more lightweight
and assumes all required native functionality is implemented in Swift.

Generating the Xcode project can be done using the `xcodeGen` task. This will start
[XcodeGen](https://github.com/yonaskolb/XcodeGen), which means that XcodeGen needs to be available
when using this task.

The following configuration options are available via the `xcode` section:

| Name                  | Required | Description                                                       |
|-----------------------|----------|-------------------------------------------------------------------|
| `appId`               | yes      | App ID in the form MyApp.                                         |
| `bundleId`            | yes      | Apple bundleID in the form com.example.                           |
| `appName`             | yes      | App display name in the form My App.                              |
| `bundleVersion`       | yes      | Version number in the form 1.2.3.                                 |
| `icon`                | yes      | PNG file that will be used to generate the app icons.             |
| `iconBackgroundColor` | no       | Color used to replace the icon alpha channel, e.g. #000000.       |
| `resourcesDir`        | yes      | Directory to copy into the app's resources.                       |
| `launchScreenColor`   | no       | Background color for the app's launch screen.                     |
| `outputDir`           | no       | Directory for the Xcode project, defaults to `build/xcode`.       |
| `xcodeGenPath`        | no       | XcodeGen install location, defaults to `/usr/local/bin/xcodegen`. |

Like the Mac application bundle, the `buildversion` system property can be used to set the build
version during the build. If this system property is not present, the build version is the same
as the app version.

### Communication between native code and JavaScript

It is common for hybrid mobile apps to feature some kind of interaction between the native code
and the web application's JavaScript. The generated Xcode project therefore includes some bindings
for common tasks, which can be called from JavaScript:

- `clrz.openNativeBrowser(url)` opens a link in the native browser (i.e. Mobile Safari),
  rather than in the web view. Note that external links will be opened in the native browser
  by default.
- `clrz.loadPreferences()` will load the native app store into the web application's
  [local storage](https://developer.mozilla.org/en-US/docs/Web/API/Window/localStorage). This can
  be used for persistent data, since iOS aggressively cleans up local storage for infrequently
  used apps.
- `clrz.savePreferences(name, value)` saves a name/value pairs to the native app storage.
- `clrz.requestNotifications()` requests permission to schedule notifications. If the user has
  already previously approved or rejected permission, calling this function does nothing.
- `clrz.scheduleNotification(id, title, preview, schedule)` schedules a native notification for
  the specified date and time. The `schedule` argument should be a date/time in ISO 8601 format,
  for example "2024-10-07 10:37:00". The date is interpreted against the user's current time zone.
- `clrz.cancelNotification(id)` cancels a previously scheduled notification.

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

| Name            | Required | Description                                                      |
|-----------------|----------|------------------------------------------------------------------|
| `webAppDir`     | yes      | Input directory where the original web app is located.           |
| `outputDir`     | no       | Output directory for the generated PWA, defaults to `build/pwa`. |
| `manifest`      | yes      | Location of the web app manifest JSON file.                      |
| `serviceWorker` | no       | Location of service worker file, will use default if omitted.    |
| `cacheName`     | yes      | Name of the cache the service worker will use.                   |

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

- [Java JDK](http://java.oracle.com) 21+
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

Testing the plugin with the example application
-----------------------------------------------

The plugin comes with an example application, that can be used to test the plugin on itself:

- First run `gradle assemble` to build the plugin itself.
- Navigate to the `example` directory to build the example app.
  - Run `gradle createApplicationBundle` to create a Mac application bundle.
  - Run `gradle signApplicationBundle` to sign a Mac application bundle.
  - Run `gradle packageApplicationBundle` to create a Mac application bundle using `jpackage`.
  - Run `gradle packageMSI` to create a Windows MSI installer.
  - Run `gradle packageEXE` to create a standalone Windows application.
  - Run `gradle xcodeGen` to generate a Xcode project for a hybrid iOS app.
  - Run `gradle generateStaticSite` to generate a website from Markdown templates.
  - Run `gradle generatePWA` to create a PWA version of the aforementioned website.
    
Building the example application uses the same system requirements and environment variables as
the plugin itself. Refer to the documentation for each application type for details.
  
License
-------

Copyright 2010-2025 Colorize

> Licensed under the Apache License, Version 2.0 (the "License");
> you may not use this file except in compliance with the License.
> You may obtain a copy of the License at
>
>    http://www.apache.org/licenses/LICENSE-2.0
>
> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.
