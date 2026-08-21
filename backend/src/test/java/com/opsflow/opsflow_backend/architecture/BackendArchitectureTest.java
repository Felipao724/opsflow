package com.opsflow.opsflow_backend.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.modules.syntax.ModuleRuleDefinition.modules;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

class BackendArchitectureTest {

    private static final String BASE_PACKAGE = "com.opsflow.opsflow_backend";

    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(BASE_PACKAGE);

    @Test
    void platformMustNotDependOnBusinessModules() {
        noClasses()
                .that().resideInAPackage(BASE_PACKAGE + ".platform..")
                .should().dependOnClassesThat()
                .resideInAPackage(BASE_PACKAGE + ".modules..")
                .because("technical infrastructure must remain independent from business capabilities")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void sharedKernelMustRemainIndependent() {
        noClasses()
                .that().resideInAPackage(BASE_PACKAGE + ".sharedkernel..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        BASE_PACKAGE + ".modules..",
                        BASE_PACKAGE + ".platform..",
                        "org.springframework..",
                        "jakarta..")
                .because("shared kernel concepts must remain stable and framework-independent")
                .allowEmptyShould(true)
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void modulesMustOnlyCommunicateThroughTheirApis() {
        modules()
                .definedByPackages(BASE_PACKAGE + ".modules.(*)..")
                .should().onlyDependOnEachOtherThroughClassesThat()
                .resideInAPackage(BASE_PACKAGE + ".modules.*.api..")
                .because("a module must not access the internal implementation of another module")
                .allowEmptyShould(true)
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void businessModulesMustBeFreeOfCycles() {
        modules()
                .definedByPackages(BASE_PACKAGE + ".modules.(*)..")
                .should().beFreeOfCycles()
                .because("business modules must remain independently evolvable")
                .allowEmptyShould(true)
                .check(PRODUCTION_CLASSES);
    }

}