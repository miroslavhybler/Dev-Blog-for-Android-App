import com.android.tools.r8.internal.ks
import kotlin.collections.set

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.androidx.baselineprofile)
    alias(libs.plugins.aboutLibraries)
    alias(libs.plugins.kotlin.serialization)
}


android {
    namespace = "com.jet.article.example.devblog"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jet.article.example.devblog"
        minSdk = 24
        targetSdk = 36
        versionCode = 12
        versionName = "1.2.0"

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
    debugImplementation(dependencyNotation = files("libs/jet-article-debug.aar"))
    releaseImplementation(dependencyNotation = files("libs/jet-article-release.aar"))
    implementation(dependencyNotation = project(path = ":tests-names"))

    implementation(dependencyNotation = libs.jet.utils)
    implementation(dependencyNotation = libs.jet.lint)
    implementation(dependencyNotation = libs.jet.tts)

    implementation(dependencyNotation = libs.androidx.core.core.ktx)
    implementation(dependencyNotation = libs.androidx.lifecycle.lifecycle.runtime.ktx)
    implementation(dependencyNotation = libs.androidx.activity.compose)
    //implementation(platform(libs.androidx.compose.bom))
    implementation(dependencyNotation = libs.androidx.datastore.preferences.core)
    implementation(dependencyNotation = libs.androidx.datastore.preferences)
    implementation(dependencyNotation = libs.androidx.ui.tooling.preview)


    implementation(dependencyNotation = libs.androidx.ui)
    implementation(dependencyNotation = libs.androidx.ui.graphics)
    implementation(dependencyNotation = libs.ui.tooling.preview)
    implementation(dependencyNotation = libs.material)
    implementation(dependencyNotation = libs.material3)
    implementation(dependencyNotation = libs.androidx.adaptive.android)
    implementation(dependencyNotation = libs.androidx.palette.ktx)
    implementation(dependencyNotation = libs.androidx.work.runtime.ktx)
    implementation(dependencyNotation = libs.androidx.core.splashscreen)


    implementation(dependencyNotation = libs.androidx.paging.runtime.ktx)
    testImplementation(dependencyNotation = libs.androidx.paging.paging.common.ktx)
    implementation(dependencyNotation = libs.androidx.paging.compose)

    implementation(dependencyNotation = libs.android.joda)
    implementation(dependencyNotation = libs.aboutlibraries.compose)
    implementation(dependencyNotation = libs.coil.compose)
    implementation(dependencyNotation = libs.coil.gif)

    /** Adaptive UI */
    implementation(dependencyNotation = libs.androidx.adaptive)
    implementation(dependencyNotation = libs.androidx.adaptive.layout)
    implementation(dependencyNotation = libs.androidx.adaptive.navigation)
    implementation(dependencyNotation = libs.androidx.adaptive.navigation.suite)

    /** Navigation 3 */
    implementation(dependencyNotation = libs.androidx.material3.navigation3)
    implementation(dependencyNotation = libs.androidx.navigation3.ui)
    implementation(dependencyNotation = libs.androidx.navigation3.runtime)
    implementation(dependencyNotation = libs.androidx.lifecycle.viewmodel.navigation3)

    /** Room Database */
    implementation(dependencyNotation = libs.androidx.room.runtime)
    implementation(dependencyNotation = libs.androidx.room.ktx)
    implementation(dependencyNotation = libs.androidx.datastore.core.android)
    implementation(dependencyNotation = libs.androidx.appcompat)
    implementation(dependencyNotation = libs.androidx.storage)
    implementation(dependencyNotation = libs.androidx.profileinstaller)
    baselineProfile(dependencyNotation = project(path = ":baselineprofile"))
    debugImplementation(dependencyNotation = libs.ui.tooling)
    ksp(dependencyNotation = libs.androidx.room.compiler)

    /** InApp updates*/
    implementation(dependencyNotation = libs.app.update.ktx)

    /** KTOR */
    implementation(dependencyNotation = libs.ktor.client.android)
    implementation(dependencyNotation = libs.ktor.client.serialization)
    implementation(dependencyNotation = libs.ktor.client.logging.jvm)

    /** Hilt DI */
    implementation(dependencyNotation = libs.google.dagger.hilt)
    implementation(dependencyNotation = libs.androidx.hilt.common)
    ksp(dependencyNotation = libs.google.dagger.hilt.compiler)
    implementation(dependencyNotation = libs.androidx.hilt.navigation.compose)
    implementation(dependencyNotation = libs.androidx.hilt.work)


    /** Tests */
    implementation(dependencyNotation = libs.androidx.runtime.tracing)
    testImplementation(dependencyNotation = libs.junit)
    androidTestImplementation(dependencyNotation = libs.androidx.junit)
    androidTestImplementation(dependencyNotation = libs.androidx.espresso.core)
    //  androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(dependencyNotation = libs.androidx.ui.test.junit4)
    debugImplementation(dependencyNotation = libs.ui.tooling)
    debugImplementation(dependencyNotation = libs.androidx.ui.test.manifest)
}