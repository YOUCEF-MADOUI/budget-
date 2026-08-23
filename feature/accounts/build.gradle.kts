plugins {
    id("budgetplusplus.android.feature")
    id("budgetplusplus.android.hilt")
}
dependencies {
    implementation(project(":domain"))
    implementation(project(":core:model"))
    implementation(libs.androidx.hilt.navigation.compose)
}
