import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    id("org.jetbrains.intellij") version "1.15.0"
    id("org.jetbrains.changelog") version "2.1.2" // "1.2.1"  // newer versions are broken
    id("com.github.ben-manes.versions") version "0.47.0"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.9.3")
}

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
    downloadSources.set(properties("platformDownloadSources").toBoolean())
    updateSinceUntilBuild.set(true)

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(
        properties("platformPlugins")
            .split(',')
            .map(String::trim)
            .filter(String::isNotEmpty))
}

// Configure gradle-changelog-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(properties("pluginVersion"))
    groups.empty()
    headerParserRegex.set("""([1-9][0-9][0-9][0-9])\.(\d+)(\.\d+)?(-([A-Z]+)(\.(\d+))?)?""")
}

tasks {
    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = it
            kotlinOptions.languageVersion = "1.4"
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }

    buildSearchableOptions {
      enabled = false
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        // set to empty to include future builds
        // untilBuild.set(properties("pluginUntilBuild"))
        untilBuild.set("")

        // Extract the <!-- Plugin description --> section from README.md and set it in the
        // plugin's manifest
        pluginDescription.set(
            File(projectDir, "README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )

        // Get the latest available change notes from the changelog file
//        changeNotes.set(provider { changelog.getLatest().toHTML() })
        changeNotes.set(provider { changelog.renderItem(changelog.getLatest(), Changelog.OutputType.HTML) })
    }

    runPluginVerifier {
        ideVersions.set(
            properties("pluginVerifierIdeVersions")
                .split(',')
                .map(String::trim)
                .filter(String::isNotEmpty))
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("INTELLIJ_PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels
        // like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically.
        // Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels.set(listOf(
            properties("pluginVersion")
                .split('-')
                .getOrElse(1) { "default" }
                .split('.')
                .first()))
    }
}
