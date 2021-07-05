plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android")
}

android {
    compileSdk = 30

    defaultConfig {
        applicationId = "dk.appdo.calendarx"
        minSdk = (26)
        targetSdk = (30)
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        getByName("debug") {
            versionNameSuffix = "-debug"
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.COMPOSE
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("../debug.keystore")
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storePassword = "android"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
    }
}

dependencies {

    implementation(Libs.KOTLIN_STD)
    implementation(Libs.KOTLIN_COROUTINES)

    implementation(Libs.COMPOSE_ANIMATION)
    implementation(Libs.COMPOSE_FOUNDATION)
    implementation(Libs.COMPOSE_LAYOUT)
    implementation(Libs.COMPOSE_MATERIAL)
    implementation(Libs.COMPOSE_RUNTIME)
    implementation(Libs.COMPOSE_TOOLING)
    implementation(Libs.COMPOSE_UI)
    implementation(Libs.COMPOSE_PAGING)
    implementation(Libs.MDC_COMPOSE_THEME_ADAPTER)
    implementation(Libs.MATERIAL)

    implementation(Libs.CORE_KTX)


    implementation(Libs.ACTIVITY_COMPOSE)
    implementation(Libs.ACTIVITY_KTX)
    implementation(Libs.APPCOMPAT)
    implementation(Libs.LIFECYCLE_RUNTIME_KTX)
}
