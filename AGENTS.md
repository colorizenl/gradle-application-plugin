# AGENTS.md

This file provides instructions to agents working on this repository.

## Project summary

This is a Gradle plugin that is used to generate native applications for various platforms.

## Commands

- `gradle test` runs all unit tests.
- `gradle test --tests '*SomethingTest'` runs unit tests in a specific class.
- `gradle spotbugsMain` runs the SpotBugs quality checks.
- `gradle assemble` does a full build.

## Architecture

- Every target platform comes with its own tasks and its own configuration. 
- Every target platform is located in its own sub-package in `nl.colorize.gradle.application`.

## Code style

- Use the UTF-8 character encoding, 4 space indent, Unit line breaks.
- In general, code style is not very strict and not enforced. Try to follow a reasonable style.
- Maximum line length is ideally 100 characters, but this can be ignored if necessary.
- Use Lombok annotations to reduce boilerplate code.
