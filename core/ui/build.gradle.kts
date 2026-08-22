plugins {
    id("budgetplusplus.android.library")
    id("budgetplusplus.android.compose")
}

dependencies {
    api(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
}
