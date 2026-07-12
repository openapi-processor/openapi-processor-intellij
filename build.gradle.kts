import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.changelog")
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit4)
    testRuntimeOnly(libs.junit.launcher)
//    testImplementation("junit:junit:4.13.2")

    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more:
    // https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
//        intellijIdea("2025.2.6.2")
        intellijIdea("2026.1")

        // Plugin Dependencies - https://plugins.jetbrains.com/docs/intellij/plugin-dependencies.html
        bundledPlugins("com.intellij.java", "org.jetbrains.plugins.yaml", "com.intellij.modules.json")

        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.JUnit5)
        testFramework(TestFrameworkType.Plugin.Java)
    }
}

// Configure IntelliJ Platform Gradle Plugin - read more:
// https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html
intellijPlatform {
    buildSearchableOptions = false

    publishing {
        token = providers.environmentVariable("INTELLIJ_PUBLISH_TOKEN")

        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = if (isSnapshot()) listOf("SNAPSHOT") else listOf("default")
    }

    pluginVerification {
        freeArgs = listOf(
          "-mute",
          "TemplateWordInPluginId"
        )
    }
}

// Configure Gradle Changelog Plugin - read more:
// https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
    headerParserRegex = """^((2[0-9]{3})\.(\d+)(\.(\d+))?(-(SNAPSHOT\.(\d+)))?)$"""
    header = provider { version.get() }
    combinePreReleases = false
    versionPrefix = ""
}

fun isSnapshot() = version.toString().contains("-SNAPSHOT")
