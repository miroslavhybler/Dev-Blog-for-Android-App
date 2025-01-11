plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false

    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    id("org.jetbrains.dokka") version "1.9.20" apply false
    id("androidx.benchmark") version "1.3.3" apply false
    alias(libs.plugins.aboutLibraries) apply false

    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.google.dagger.hilt) apply false
    alias(libs.plugins.androidx.baselineprofile) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
}