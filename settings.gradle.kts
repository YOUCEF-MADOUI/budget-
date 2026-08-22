pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "BudgetPlusPlus"

include(
    ":app",
    ":core:common",
    ":core:model",
    ":core:designsystem",
    ":core:ui",
    ":core:security",
    ":core:testing",
    ":domain",
    ":database",
    ":data",
    ":feature:onboarding",
    ":feature:dashboard",
    ":feature:accounts",
    ":feature:transactions",
    ":feature:categories",
    ":feature:budgets",
    ":feature:recurring",
    ":feature:analytics",
    ":feature:search",
    ":feature:backup",
    ":feature:settings",
    ":feature:security",
    ":feature:futurepayments",
    ":feature:favorites",
)
