plugins {
    alias(libs.plugins.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kapt)
    alias(libs.plugins.kotlin)
}

android {
    namespace = "me.goldhardt.woderful"
    compileSdk = 34

    defaultConfig {
        applicationId = "me.goldhardt.woderful"
        minSdk = 33
        targetSdk = 33
        versionCode = 2
        versionName = "0.1.0"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core
    implementation(libs.core.ktx)
    implementation(libs.playservices.wearable)
    implementation(libs.legacy.support.v4)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.navigation.runtime.ktx)
    implementation(libs.guava)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui.base)
    implementation(libs.ui.tooling.preview)
    implementation(libs.ui.tooling)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Wear OS
    implementation(libs.wear.foundation)
    implementation(libs.wear.navigation)
    implementation(libs.wear.material)

    // Accompanist
    implementation(libs.accompanist.permissions)

    // Lifecycle
    implementation(libs.concurrent.futures.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)

    // Horologist
    implementation(libs.horologist.composables)
    implementation(libs.horologist.compose.tools)
    implementation(libs.horologist.tiles)
    implementation(libs.horologist.health.composables)
    implementation(libs.horologist.health.service)
    implementation(libs.horologist.compose.layout)

    // Material
    implementation(libs.compose.material.base)
    implementation(libs.material.icons.extended)

    // Health Services
    implementation(libs.health.services.client)
    implementation(libs.wear.ongoing)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    kapt(libs.hilt.compiler)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    annotationProcessor(libs.room.compiler)
     kapt(libs.room.compiler)

    // Paging
    implementation(libs.paging.runtime.ktx)
    implementation(libs.paging.compose)

    // DataStore
    implementation(libs.datastore.preferences)

    // Json
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)

    // Tests
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}