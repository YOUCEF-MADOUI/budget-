package com.budgetplusplus.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.android")
        configureAndroidLibrary()

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        dependencies.add("testImplementation", libs.findLibrary("junit4").get())
          }
    }
}
