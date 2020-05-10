Mac application bundle plugin for Gradle
========================================

Gradle plugin that can be used to create an
[application bundle](https://en.wikipedia.org/wiki/Bundle_(macOS) for Mac OS as part of the build. 
This plugin is based on the [Ant plugin by TheInfiniteKind](https://github.com/TheInfiniteKind/appbundler), 
but makes it available via Gradle. It also adds the ability to sign the application bundle and
package it within an installer.

Usage
-----

The plugin is available from the [Gradle plugin registry](https://plugins.gradle.org). Adding the
plugin to the build is done by adding the following to `build.gradle`:

    plugins {
        id "nl.colorize.gradle.macapplicationbundle" version "2020.6"
    }
    
The plugin will create two tasks: `createApplicationBundle` and `signApplicationBundle`. Note that
the tasks are *not* added to any standard tasks such as `assemble`, as Mac application bundles
can only be created when running the build on a Mac, making the tasks incompatible with some
workflows (i.e. continuous integration pipelines that tend to use Linux servers). 

The tasks can be configured using the `macApplicationBundle` block. The configuration
options correspong to the ones from the [Ant plugin](https://github.com/TheInfiniteKind/appbundler).
The following shows an example on how to define this configuration in Gradle:

    macApplicationBundle {
        name = "Example"
        identifier = "com.example"
        description = "A description for your application"
        copyright = "Copyright 2020"
        version = "1.0"
        icon = "resources/icon.icns"
        applicationCategory = "public.app-category.developer-tools"
        mainClassName = "com.example.app.Main"
        contentDir = "${buildDir}/jarfiles"
        outputDir = "${buildDir}"
        signIdentityApp = "your signing identity"
        signIdentityInstaller = "your signing identity"
    }
    
The plugin requires the `JAVA_HOME` environment variable to be set, as it needs to add an embedded
version of Java to the application bundle.

Signing the application bundle is mandatory for distributing the application. This in turn needs
a valid Apple developer account, and corresponding certificates. You will need two certificates:
one for the application, and one for the installer. Please do not hard-code the signing identity
into the build file. It is better to define them in the Gradle properties file at
`~/.gradle/gradle.properties` and then access them from there.   

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
