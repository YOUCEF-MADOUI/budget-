plugins {
    id("budgetplusplus.android.library")
    id("budgetplusplus.android.hilt")
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:security"))
    implementation(project(":domain"))
    implementation(project(":database"))
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    implementation(libs.kotlinx.coroutines.android)
}
