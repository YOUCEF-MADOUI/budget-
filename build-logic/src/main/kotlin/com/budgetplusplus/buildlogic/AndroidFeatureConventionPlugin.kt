package com.budgetplusplus.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
        pluginManager.apply("budgetplusplus.android.library")
        pluginManager.apply("budgetplusplus.android.compose")

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        dependencies.add("implementation", project(":core:designsystem"))
        dependencies.add("implementation", project(":core:ui"))
        dependencies.add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
        dependencies.add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
        dependencies.add("testImplementation", dependencies.platform(libs.findLibrary("androidx-compose-bom").get()))
        dependencies.add("testImplementation", libs.findLibrary("androidx-compose-ui-test-junit4").get())
        dependencies.add("testImplementation", libs.findLibrary("androidx-test-core").get())
        dependencies.add("testImplementation", libs.findLibrary("robolectric").get())
        dependencies.add("debugImplementation", libs.findLibrary("androidx-compose-ui-test-manifest").get())
          }
    }
}
