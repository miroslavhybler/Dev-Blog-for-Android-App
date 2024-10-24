plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {

    namespace = "com.jet.article.example.devblog.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
        targetSdk = 34
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    kotlin {
        jvmToolchain(jdkVersion = 8)
    }
}