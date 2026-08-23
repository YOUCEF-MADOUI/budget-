plugins {
    id("budgetplusplus.android.library")
    id("budgetplusplus.android.room")
    id("budgetplusplus.android.hilt")
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.matching { it.name == "testReleaseUnitTest" }.configureEach {
    (this as org.gradle.api.tasks.testing.Test).exclude("**/PerformanceScaleTest*")
}
