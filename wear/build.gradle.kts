plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.qibla.prayertimes.wear"
    compileSdk = 34

    defaultConfig {
        // Deliberately its own applicationId (not the phone app's) — this is a standalone Wear
        // OS app (see the "standalone" metadata in AndroidManifest.xml), installed and run
        // independently of the phone app, not launched as its companion.
        applicationId = "com.qibla.prayertimes.wear"
        minSdk = 28 // Wear OS 3+
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Same qibla-bearing math and compass-sensor code the phone app uses — see :shared.
    implementation(project(":shared"))

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")

    // Wear-specific Compose widgets (Text, Chip, CircularProgressIndicator, etc.) styled for
    // round/square watch screens.
    implementation("androidx.wear.compose:compose-material:1.3.1")
    implementation("androidx.wear.compose:compose-foundation:1.3.1")
    implementation("androidx.wear:wear-tooling-preview:1.0.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
