//-----------------------------------------------------------------------------
// Gradle Mac Application Bundle Plugin
// Copyright 2010-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.example;

/**
 * An example application that does nothing except printing a message to stdout.
 * This class only exists so that the plugin is able to build itself as an
 * application, which requires a main method.
 */
public class ExampleApp {

    public static void main(String[] args) {
        System.out.println("Example");
    }
}
