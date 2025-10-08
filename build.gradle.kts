// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// Build with JDK 17 everywhere (local + CI)
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
