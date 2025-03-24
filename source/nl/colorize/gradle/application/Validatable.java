//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application;

/**
 * Marker interface for all configuration sections in the plugin. Can be used
 * to make sure all required options are provided and have acceptable values.
 */
public interface Validatable {

    public void validate();
}
