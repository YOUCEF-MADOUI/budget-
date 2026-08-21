plugins {
    id("budgetplusplus.android.library")
    id("budgetplusplus.android.room")
    id("budgetplusplus.android.hilt")
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
}
