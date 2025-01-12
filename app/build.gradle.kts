import com.android.tools.r8.internal.ks
import kotlin.collections.set

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.google.dagger.hilt)
    alias(libs.plugins.androidx.baselineprofile)
    alias(libs.plugins.aboutLibraries)
}

android {
    namespace = "com.jet.article.example.devblog"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.jet.article.example.devblog"
        minSdk = 24
        targetSdk = 35
        versionCode = 10
        versionName = "1.1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
        create("benchmark1") {
            initWith(buildTypes.getByName("release"))
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    kotlin {
        jvmToolchain(jdkVersion = 11)
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    ksp {
        arg(k = "room.schemaLocation", v = "${projectDir.path}/room-schemas")
    }
}

dependencies {

//    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))

    //debugImplementation(files("libs/jet-article-debug.aar"))
    debugImplementation(files("libs/jet-article-debug.aar"))
    releaseImplementation(files("libs/jet-article-release.aar"))
    implementation(project(":tests-names"))
    implementation(libs.jet.utils)
    implementation(libs.jet.lint)

    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.lifecycle.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.datastore.preferences.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.ui.tooling.preview)


    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material)
    implementation(libs.material3)
    implementation(libs.androidx.adaptive.android)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.android.joda)
    implementation(libs.aboutlibraries.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

    /** Adaptive UI */
    implementation(libs.androidx.adaptive)
    implementation(libs.androidx.adaptive.layout)
    implementation(libs.androidx.adaptive.navigation)
    implementation(libs.androidx.adaptive.navigation.suite)

    /** Room Database */
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.datastore.core.android)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.storage)
    implementation(libs.androidx.profileinstaller)
    baselineProfile(project(":baselineprofile"))
    debugImplementation(libs.ui.tooling)
    ksp(libs.androidx.room.compiler)

    /** KTOR */
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.client.logging.jvm)

    /** Hilt DI */
    implementation(libs.google.dagger.hilt)
    implementation(libs.androidx.hilt.common)
    ksp(libs.google.dagger.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)


    /** Tests */
    implementation(libs.androidx.runtime.tracing)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}