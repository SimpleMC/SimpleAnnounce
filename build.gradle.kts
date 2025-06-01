import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import pl.allegro.tech.build.axion.release.domain.hooks.HookContext
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.axionRelease)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.shadow)
}

scmVersion {
    versionIncrementer("incrementMinorIfNotOnRelease", mapOf("releaseBranchPattern" to "release/.+"))
    unshallowRepoOnCI.set(true)

    hooks {
        // Automate moving `[Unreleased]` changelog entries into `[<version>]` on release
        // FIXME - workaround for Kotlin DSL issue https://github.com/allegro/axion-release-plugin/issues/500
        val changelogPattern =
            "\\[Unreleased\\]([\\s\\S]+?)\\n" +
                "(?:^\\[Unreleased\\]: https:\\/\\/github\\.com\\/(\\S+\\/\\S+)\\/compare\\/[^\\n]*\$([\\s\\S]*))?\\z"
        pre(
            "fileUpdate",
            mapOf(
                "file" to "CHANGELOG.md",
                "pattern" to KotlinClosure2<String, HookContext, String>({ _, _ -> changelogPattern }),
                "replacement" to KotlinClosure2<String, HookContext, String>({ version, context ->
                    // github "diff" for previous version
                    val previousVersionDiffLink =
                        when (context.previousVersion == version) {
                            true -> "releases/tag/v$version" // no previous, just link to the version
                            false -> "compare/v${context.previousVersion}...v$version"
                        }
                    """
                        \[Unreleased\]

                        ## \[$version\] - $currentDateString$1
                        \[Unreleased\]: https:\/\/github\.com\/$2\/compare\/v$version...HEAD
                        \[$version\]: https:\/\/github\.com\/$2\/$previousVersionDiffLink$3
                    """.trimIndent()
                }),
            ),
        )

        pre("commit")
    }
}

group = "org.simplemc"
version = scmVersion.version

val currentDateString: String
    get() = OffsetDateTime.now(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ISO_DATE)

kotlin {
    jvmToolchain(21)
}

val shadowJarOnly: Boolean = project.property("shadowJarOnly")?.toString()?.toBoolean() ?: false
val runtimeClasspath by configurations.runtimeClasspath

dependencies {
    compileOnly(libs.spigot)
    implementation(libs.kotlinLogger)
    implementation(libs.jacksonKotlin)
    implementation(libs.jacksonDataformatYaml)
    implementation(kotlin("stdlib"))

    testImplementation(libs.spigot)
}

tasks {
    wrapper {
        distributionType = Wrapper.DistributionType.ALL
    }

    processResources {
        inputs.property("shadowJarOnly", shadowJarOnly)

        // inject "online" libraries into online plugin variant
        val libraries = runtimeClasspath.resolvedConfiguration.resolvedArtifacts
            .joinToString("\n  - ", prefix = "\n  - ") { artifact ->
                val id = artifact.moduleVersion.id
                "${id.group}:${id.name}:${id.version}"
            }

        val placeholders = mapOf(
            "version" to version,
            "apiVersion" to libs.versions.mcApi.get(),
            "libraries" to libraries,
        )

        filesMatching("plugin.yml") {
            expand(placeholders)
        }

        // create an "offline" copy/variant of the plugin.yml with `libraries` omitted
        doLast {
            val resourcesDir = sourceSets.main.get().output.resourcesDir
            val yamlDumpOptions =
                // make it pretty for the people
                DumperOptions().also {
                    it.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
                    it.isPrettyFlow = true
                }
            val yaml = Yaml(yamlDumpOptions)
            val pluginYml: Map<String, Any> = yaml.load(file("$resourcesDir/plugin.yml").inputStream())
            yaml.dump(pluginYml.filterKeys { it != "libraries" }, file("$resourcesDir/offline-plugin.yml").writer())
        }
    }

    // offline jar should be ready to go with all dependencies
    shadowJar {
        mergeServiceFiles()
        minimize {
            // if present, kotlin-reflect must be excluded from minimization
            exclude(dependency("org.jetbrains.kotlin:kotlin-reflect"))
        }
        archiveClassifier.set(if (shadowJarOnly) "" else "offline")
        exclude("plugin.yml")
        rename("offline-plugin.yml", "plugin.yml")

        // avoid classpath conflicts/pollution via relocation
        isEnableRelocation = true
        relocationPrefix = "${project.group}.${project.name.lowercase()}.libraries"

        // if using reflection, don't relocate kotlin:
        // shadow relocation doesn't relocate certain metadata breaking some synthetic classes in the case of reflection (used by jackson, for example)
        // see also: https://github.com/JetBrains/Exposed/issues/1353
        if (runtimeClasspath.resolvedConfiguration.resolvedArtifacts.any { it.name == "kotlin-reflect" }) {
            logger.warn("Detected kotlin-reflect in runtime classpath, not relocating kotlin! Proceed with caution.")
            relocate("kotlin", "kotlin")
        }
    }

    build { dependsOn(shadowJar) }
}
