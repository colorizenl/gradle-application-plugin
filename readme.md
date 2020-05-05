Mac application bundle plugin for Gradle
========================================

Gradle plugin that can be used to create an [application bundle](https://en.wikipedia.org/wiki/Bundle_(macOS)
for Mac OS as part of the build. This plugin is based on the 
[Ant plugin by TheInfiniteKind](https://github.com/TheInfiniteKind/appbundler), but makes it
available via Gradle.

Usage
-----

The plugin is available from the [Gradle plugin registry](https://plugins.gradle.org). Adding the
plugin to the build is done by adding the following to `build.gradle`:

    plugins {
        id "nl.colorize.gradle.macapplicationbundle" version "2020.2"
    }
    
This will add a task `createApplicationBundle` to the build. When this task is used, i.e. by
running `gradle createApplicationBundle`, it will generate the application bundle. The configuration
options are the same as the [Ant plugin](https://github.com/TheInfiniteKind/appbundler). The
following shows an example on how to configure it from Gradle:

    macApplicationBundle {
        name = "Example"
        identifier = "com.example"
        description = "A description for your application"
        copyright = "Copyright 2020"
        version = "1.0"
        icon = "resources/icon.icns"
        applicationCategory = "public.app-category.developer-tools"
        mainclassname = "com.example.app.Main"
        contentDir = "${buildDir}/jarfiles"
    }
    
The plugin requires the `JAVA_HOME` environment variable to be set.

Build instructions
------------------

Building the plugin itself can only be done on Mac OS. It also requires the following:

- [Java JDK](http://java.oracle.com) 8 or 11+
- [Gradle](http://gradle.org)
- [Ant](https://ant.apache.org)

The following Gradle build tasks are available:

- `gradle clean` cleans the build directory
- `gradle assemble` creates the JAR file for distribution
- `gradle test` runs all unit tests
- `gradle coverage` runs all unit tests and reports on test coverage
  
License
-------

Copyright 2010-2020 Colorize

The source code is licensed under the Apache License. Refer to
[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0) for
the full license text.
