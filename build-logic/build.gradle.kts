plugins {
    `kotlin-dsl`
}

group = "com.budgetplusplus.buildlogic"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.compose.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
    implementation(libs.hilt.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "budgetplusplus.android.application"
            implementationClass = "com.budgetplusplus.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "budgetplusplus.android.library"
            implementationClass = "com.budgetplusplus.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "budgetplusplus.android.compose"
            implementationClass = "com.budgetplusplus.buildlogic.AndroidComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "budgetplusplus.android.feature"
            implementationClass = "com.budgetplusplus.buildlogic.AndroidFeatureConventionPlugin"
        }
        register("androidRoom") {
            id = "budgetplusplus.android.room"
            implementationClass = "com.budgetplusplus.buildlogic.AndroidRoomConventionPlugin"
        }
        register("androidHilt") {
            id = "budgetplusplus.android.hilt"
            implementationClass = "com.budgetplusplus.buildlogic.AndroidHiltConventionPlugin"
        }
        register("kotlinLibrary") {
            id = "budgetplusplus.kotlin.library"
            implementationClass = "com.budgetplusplus.buildlogic.KotlinLibraryConventionPlugin"
        }
    }
}
