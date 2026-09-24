plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val releaseSigningEnvironment = mapOf(
    "storeFile" to System.getenv("WOL_RELEASE_STORE_FILE"),
    "storePassword" to System.getenv("WOL_RELEASE_STORE_PASSWORD"),
    "keyAlias" to System.getenv("WOL_RELEASE_KEY_ALIAS"),
    "keyPassword" to System.getenv("WOL_RELEASE_KEY_PASSWORD"),
)
val releaseSigningComplete = releaseSigningEnvironment.values.all { !it.isNullOrBlank() }

gradle.taskGraph.whenReady {
    if (allTasks.any { it.name.contains("release", ignoreCase = true) } && !releaseSigningComplete) {
        error(
            "Release signing requires WOL_RELEASE_STORE_FILE, WOL_RELEASE_STORE_PASSWORD, " +
                "WOL_RELEASE_KEY_ALIAS, and WOL_RELEASE_KEY_PASSWORD.",
        )
    }
}

android {
    namespace = "de.sqzr2k.wol"
    compileSdk = 36
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "de.sqzr2k.wol"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (releaseSigningComplete) {
                storeFile = file(checkNotNull(releaseSigningEnvironment["storeFile"]))
                storePassword = checkNotNull(releaseSigningEnvironment["storePassword"])
                keyAlias = checkNotNull(releaseSigningEnvironment["keyAlias"])
                keyPassword = checkNotNull(releaseSigningEnvironment["keyPassword"])
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.kotlinx.coroutines.android)

    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
