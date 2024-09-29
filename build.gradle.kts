plugins {
    alias(libs.plugins.android.application) apply false
    id("com.android.library") version "8.6.1" apply false
    id("com.android.test") version "8.6.1" apply false

    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("org.jetbrains.dokka") version "1.9.20" apply false
    id("androidx.benchmark") version "1.3.0" apply false
    id("com.mikepenz.aboutlibraries.plugin") version "11.2.3" apply false

    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.google.dagger.hilt) apply false
}