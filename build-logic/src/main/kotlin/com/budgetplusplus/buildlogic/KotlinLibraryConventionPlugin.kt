package com.budgetplusplus.buildlogic

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class KotlinLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")

        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
            toolchain.languageVersion.set(org.gradle.jvm.toolchain.JavaLanguageVersion.of(JAVA_VERSION))
        }
        extensions.configure<KotlinJvmProjectExtension> {
            jvmToolchain(JAVA_VERSION)
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
                allWarningsAsErrors.set(false)
            }
        }

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        dependencies.add("testImplementation", libs.findLibrary("junit4").get())
        tasks.withType<Test>().configureEach { useJUnit() }
          }
    }
}
