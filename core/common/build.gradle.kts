plugins { id("budgetplusplus.kotlin.library") }

dependencies {
    api(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
}
