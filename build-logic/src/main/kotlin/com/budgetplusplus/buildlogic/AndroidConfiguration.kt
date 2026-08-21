package com.budgetplusplus.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal const val COMPILE_SDK = 36
internal const val MIN_SDK = 26
internal const val TARGET_SDK = 36
internal const val JAVA_VERSION = 17

internal fun Project.configureAndroidApplication() {
    extensions.configure<ApplicationExtension> {
        namespace = "com.budgetplusplus.app"
        compileSdk = COMPILE_SDK

        defaultConfig {
            applicationId = "com.budgetplusplus.app"
            minSdk = MIN_SDK
            targetSdk = TARGET_SDK
            versionCode = 1
            versionName = "0.1.0"
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        buildTypes {
            getByName("debug") {
                applicationIdSuffix = ".debug"
                versionNameSuffix = "-debug"
            }
            getByName("release") {
                isMinifyEnabled = true
                isShrinkResources = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro",
                )
            }
        }

        packaging.resources.excludes += setOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "META-INF/LICENSE.md",
            "META-INF/LICENSE-notice.md",
        )

        testOptions.unitTests.isIncludeAndroidResources = true
    }
    configureAndroidKotlin()
}

internal fun Project.configureAndroidLibrary() {
    extensions.configure<LibraryExtension> {
        namespace = path.toBudgetNamespace()
        compileSdk = COMPILE_SDK

        defaultConfig {
            minSdk = MIN_SDK
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        testOptions.unitTests.isIncludeAndroidResources = true
    }
    configureAndroidKotlin()
}

private fun Project.configureAndroidKotlin() {
    extensions.configure<KotlinAndroidProjectExtension> {
        jvmToolchain(JAVA_VERSION)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            allWarningsAsErrors.set(false)
            freeCompilerArgs.add("-Xannotation-default-target=param-property")
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnit()
    }
}

private fun String.toBudgetNamespace(): String =
    "com.budgetplusplus" + replace(':', '.').replace('-', '.')
