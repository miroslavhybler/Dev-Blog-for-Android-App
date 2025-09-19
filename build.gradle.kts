plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false

    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    id("org.jetbrains.dokka") version "2.0.0" apply false
    id("androidx.benchmark") version "1.4.0" apply false
    alias(libs.plugins.aboutLibraries) apply false

    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.baselineprofile) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
}