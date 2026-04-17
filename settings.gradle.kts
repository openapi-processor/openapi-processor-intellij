import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

rootProject.name = "openapi-processor-intellij"

pluginManagement {
    repositories {
        maven("https://central.sonatype.com/repository/maven-snapshots")
        gradlePluginPortal()
    }

    plugins {
        // https://kotlinlang.org/docs/releases.html
        id("org.jetbrains.kotlin.jvm") version "2.3.20"
        // https://github.com/JetBrains/gradle-changelog-plugin/releases
        id("org.jetbrains.changelog") version "2.5.0"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    // https://github.com/JetBrains/intellij-platform-gradle-plugin/releases
    id("org.jetbrains.intellij.platform.settings") version "2.14.0"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenCentral()

        // IntelliJ Platform Gradle Plugin Repositories Extension - read more:
        // https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-repositories-extension.html
        intellijPlatform {
            defaultRepositories()
        }
    }
}
