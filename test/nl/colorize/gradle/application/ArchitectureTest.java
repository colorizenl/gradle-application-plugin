//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.application;

import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArchitectureTest {

    private JavaClasses classes;

    @BeforeEach
    public void before() {
        ClassFileImporter classFileImporter = new ClassFileImporter(List.of(DO_NOT_INCLUDE_TESTS));
        classes = classFileImporter.importPackages("nl.colorize.gradle.application");
    }

    @Test
    void cannotCallGetProjectInTasks() {
        String violations = classes.stream()
            .flatMap(c -> c.getAccessesFromSelf().stream())
            .filter(access -> isMethodCall(access, "org.gradle.api.DefaultTask", "getProject"))
            .map(access -> access.getOriginOwner().getName())
            .sorted()
            .collect(Collectors.joining("\n"));

        assertEquals("", violations);
    }

    @Test
    void cannotCallGetBuildDirInExtensions() {
        String violations = classes.stream()
            .flatMap(c -> c.getAccessesFromSelf().stream())
            .filter(access -> isMethodCall(access, "org.gradle.api.Project", "getBuildDir"))
            .map(access -> access.getOriginOwner().getName())
            .sorted()
            .collect(Collectors.joining("\n"));

        assertEquals("", violations);
    }

    private boolean isClassCall(JavaAccess<?> access, String toClass) {
        return access.getTargetOwner().getName().equals(toClass);
    }

    private boolean isMethodCall(JavaAccess<?> access, String toClass, String toMethod) {
        return isClassCall(access, toClass) && access.getTarget().getName().equals(toMethod);
    }
}
