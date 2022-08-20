Gradle application plugin: build applications for Mac, iOS, Android, and Windows
================================================================================

Gradle plugin that creates (native) applications for different platforms. It supports multiple
application types, for different source technologies and target platforms:

| Application type       | Supported source technology | Supported target platforms |
|------------------------|-----------------------------|----------------------------|
| Native Mac application | Java                        | Mac                        |
| Cordova app            | Web app                     | iOS, Android, Mac          |
| PWA                    | Web app                     | iOS, Android, Windows      |
| Static site            | Web app, Markdown           | Web                        |

Regardless of the approach, building mobile apps requires the development environment for the
targeted platforms:

- Building iOS and Mac apps requires [Xcode](https://developer.apple.com/xcode/)
- Building Android apps requires the [Android SDK](https://developer.android.com/sdk/index.html)

Usage
-----

The plugin is available from the [Gradle plugin registry](https://plugins.gradle.org). Adding the
plugin to the build is done by adding the following to `build.gradle`:

    plugins {
        id "nl.colorize.gradle.application" version "2022.4.5"
    }

Building native Mac applications
--------------------------------

The plugin can create Mac OS [application bundles](https://en.wikipedia.org/wiki/Bundle_%28macOS%29).
This uses the [Ant plugin by TheInfiniteKind](https://github.com/TheInfiniteKind/appbundler), 
but makes it available via Gradle. It also adds the ability to sign the application bundle and
package it within an installer.
    
The application bundle can be configured using the `macApplicationBundle` block. The configuration
options correspong to the ones from the [Ant plugin](https://github.com/TheInfiniteKind/appbundler).
The following shows an example on how to define this configuration in Gradle:

    macApplicationBundle {
        name = "Example"
        identifier = "com.example"
        description = "A description for your application"
        copyright = "Copyright 2022"
        bundleVersion = "1.0"
        icon = "resources/icon.icns"
        applicationCategory = "public.app-category.developer-tools"
        mainClassName = "com.example.app.Main"
        outputDir = "${buildDir}"
        signIdentityApp = "your signing identity"
        signIdentityInstaller = "your signing identity"
        modules = ["java.base", "java.desktop", "java.logging", "java.net.http", "jdk.crypto.ec"]
        options = ["-Xmx2g"]
        startOnFirstThread = false
    }
    
Note that, in addition to the the `bundleVersion` property, there is also the concept of build
version. This is normally the same as the bundle version, but can be manually specified for each
build by setting the `buildversion` system property. 
    
By default, the contents of the application will be based on all JAR files produces by the project,
as described by the `libsDir` property. This behavior can be replaced by setting the `contentDir`
property in the plugin's configuration. Including all application binaries, resources, and
libraries is easiest by creating a single "fat JAR" file:

    jar {
        from {
            configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) }
        }
     }
     
This might require you to add `jar.duplicatesStrategy = DuplicatesStrategy.INCLUDE` if your project
includes multiple files with identical paths.
    
The plugin adds a number of tasks to the project that use this configuration:

- **createApplicationBundle**: Creates the application bundle in the specified directory.
- **signApplicationBundle**: Signs the created application bundle and packages it into an installer
  so that it can be distributed. 

Note that the tasks are *not* added to any standard tasks such as `assemble`, as Mac application
bundles can only be created when running the build on a Mac, making the tasks incompatible with
some workflows (i.e. continuous integration pipelines that tend to use Linux servers). 
    
The plugin requires the `JAVA_HOME` environment variable to be set, as it needs to add an embedded
version of Java to the application bundle.

Signing the application bundle is mandatory for distributing the application. This in turn needs
a valid Apple developer account, and corresponding certificates. You will need two certificates:
one for the application, and one for the installer. Please do not hard-code the signing identity
into the build file. It is better to define them in the Gradle properties file at
`~/.gradle/gradle.properties` and then access them from there.

Building Cordova applications
-----------------------------

Use the `buildCordova` task to create [Cordova](https://cordova.apache.org) applications for iOS, 
Android, and Mac. The applications are generated from scratch, the intended usage is that the 
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

| Name           | Required | Description                                                          |
|----------------|----------|----------------------------------------------------------------------|
| webAppDir      | yes      | Directory containing the web application files.                      |
| outputDir      | no       | Output directory for the generated apps, default is `build/cordova`. |
| platforms      | no       | Comma-separated list of platforms, default is `ios,android,osx`.     |
| appId          | yes      | Application identifier, e.g. `nl.colorize.test`.                     |
| appName        | yes      | Application display name.                                            |
| displayVersion | yes      | Application version in the format x.y.z.                             |
| icon           | yes      | Application icon, should be a 1024x1024 PNG image.                   |
| buildJson      | yes      | Location of the Cordova `build.json` configuration file.             |
| dist           | no       | Build distribution type, either 'release' (default) or 'debug'.      |

Note that, in addition to the the `displayVersion` property, there is also the concept of build
version. This is normally the same as the display version, but can be manually specified for each
build by setting the `buildversion` system property. 

In addition to the actual build, the plugin also adds two convenience tasks, `simulateIOS` and
`simulateAndroid`, to start an iOS/Android simulator for the generated Cordova apps.

Building PWA apps
-----------------

The plugin can also repackage [PWAs](https://en.wikipedia.org/wiki/Progressive_web_application) as
native applications. The following helper tasks can be used during the creation of PWAs:

- `generatePwaServiceWorker` generates a service worker that caches the application's files.
- `generatePwaIcons` takes the application icon and generates multiple variants in different sizes.

These tasks share the same configuration via the `pwa` configuration section. The following 
configuration options are available:

| Name              | Required | Description                                                                  |
|-------------------|----------|------------------------------------------------------------------------------|
| pwaName           | yes      | PWA application name.                                                        |
| pwaVersion        | no       | PWA application version.                                                     |
| serviceWorkerFile | yes      | Output file where the generated service worker will be created.              |
| webAppDir         | yes      | Directory where the application files are located.                           |
| iconFile          | yes      | The original application file, used to generate variants in different sizes. |
| iconOutputDir     | yes      | Directory where the variants of the application icon will be saved.          |
| iconSizes         | no       | List of application icon variants to generate.                               |

Building a static site
----------------------

The plugin can take content written in Markdown or plain HTML, and then render this content using
templates to create a [static site](https://en.wikipedia.org/wiki/Static_web_page). This is done
using the `generateStaticSite` task, which uses the following logic:

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

| Name             | Required | Default       | Description                                       |
|------------------|----------|---------------|---------------------------------------------------|
| contentDir       | no       | content       | Content directory, relative to project directory. |
| outputDir        | no       | staticsite    | Output directory, relative to build directory.    |
| templateFileName | no       | template.html | File name used as template instead of content.    |

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

**Note:** Running the tests currently requires some additional JVM properties on Java 17+,
due to a [Gradle issue](https://github.com/gradle/gradle/issues/18647). These properties are
automatically used when running from Gradle, but need to be added manually when running tests 
from an IDE.
  
License
-------

Copyright 2010-2022 Colorize

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
