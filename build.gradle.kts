import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.31"
    id("com.github.johnrengelman.shadow") version "4.0.3"
}

group = "org.simplemc"
version = "1.14-SNAPSHOT"

repositories {
    jcenter()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compileOnly(group = "org.spigotmc", name = "spigot-api", version = "1.14+")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    withType<ShadowJar> {
        minimize()
    }

    named("build") {
        dependsOn(":shadowJar")
    }
}
