import build.CreateResourcesTask
import build.util.defaultVersionCatalog
import build.util.devKotlin
import build.util.excludeKotlinDependencies

plugins {
    kotlin("libs.publisher")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

// When adding new dependencies, make sure that
// kotlin-stdlib and kotlin-reflect dependencies do not get into POM file
dependencies {
    // Internal dependencies
    api(projects.api) { isTransitive = false }
    api(projects.lib) { isTransitive = false }
    api(projects.commonDependencies) { isTransitive = false }

    // Standard dependencies
    compileOnly(libs.kotlin.stable.stdlib)
    compileOnly(libs.kotlin.stable.stdlibJdk8)
    compileOnly(libs.kotlin.stable.reflect)

    // Scripting and compilation-related dependencies
    compileOnly(libs.kotlin.dev.scriptingCommon)
    compileOnly(libs.kotlin.dev.scriptingJvm)
    compileOnly(libs.kotlin.dev.scriptingCompilerImplUnshaded)

    // Serialization runtime
    compileOnly(libs.serialization.json)

    // Serialization compiler plugin (for notebooks, not for kernel code)
    compileOnly(libs.serialization.dev.unshaded)

    // Logging
    compileOnly(libs.logging.slf4j.api)

    // Clikt library for parsing output magics
    implementation(libs.clikt) {
        excludeKotlinDependencies(
            "stdlib",
            "stdlib-common",
        )
    }

    // ZeroMQ library for Jupyter messaging protocol implementation
    api(libs.zeromq)
}

buildSettings {
    withLanguageLevel(rootSettings.kotlinLanguageLevel)
    withCompilerArgs {
        skipPrereleaseCheck()
    }
    withTests()
}

CreateResourcesTask.register(project, "buildProperties", tasks.processResources) {
    addPropertiesFile(
        "kotlin-jupyter-compiler.properties",
        mapOf(
            "version" to rootSettings.pyPackageVersion,
            "kotlinVersion" to defaultVersionCatalog.versions.devKotlin,
        ),
    )

    addLibrariesFromDir(rootSettings.librariesDir)
}

kotlinPublications {
    publication {
        publicationName.set("shared-compiler")
        description.set("Implementation of REPL compiler and preprocessor for Jupyter dialect of Kotlin (IDE-compatible)")
    }
}
