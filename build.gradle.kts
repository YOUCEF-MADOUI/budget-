plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}

val staticAnalysis by tasks.registering {
    notCompatibleWithConfigurationCache("Repository-wide source policy scan")
    group = "verification"
    description = "Checks privacy, localization and repository hygiene rules."
    val sourceFiles = fileTree(rootDir) {
        include("**/*.kt", "**/*.java")
        exclude("**/build/**", "build-logic/.gradle/**")
    }
    inputs.files(sourceFiles)
    doLast {
        val forbidden = listOf(
            Regex("\\bandroid\\.util\\.Log\\.") to "Android logging is forbidden for privacy",
            Regex("\\bprintln\\s*\\(") to "println is forbidden",
            Regex("\\bText\\s*\\(\\s*\\\"") to "Compose text must use localized resources",
        )
        sourceFiles.files.forEach { file ->
            val text = file.readText()
            forbidden.forEach { (pattern, message) ->
                if (pattern.containsMatchIn(text)) throw GradleException("$message: ${file.relativeTo(rootDir)}")
            }
        }
        val forbiddenExtensions = setOf("jks", "keystore", "p12", "pfx")
        fileTree(rootDir) { exclude("**/build/**", ".git/**") }.files.firstOrNull { it.extension.lowercase() in forbiddenExtensions }?.let {
            throw GradleException("Credential file must not be versioned: ${it.relativeTo(rootDir)}")
        }
    }
}

subprojects {
    tasks.matching { it.name.startsWith("test") }.configureEach {
        dependsOn(rootProject.tasks.named("staticAnalysis"))
    }
}
