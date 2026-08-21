plugins { id("budgetplusplus.kotlin.library") }

dependencies {
    api(project(":core:model"))
    implementation(project(":core:common"))
    implementation(libs.kotlinx.coroutines.core)
}
