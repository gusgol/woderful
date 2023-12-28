plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.org.jetbrains.kotlin.kapt)
}

android {
    namespace = "me.goldhardt.woderful"
    compileSdk = 34

    defaultConfig {
        applicationId = "me.goldhardt.woderful"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
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


    implementation(libs.core.ktx)
    implementation(libs.playservices.wearable)
    implementation(libs.percent.layout)
    implementation(libs.legacySupportV4)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui.base)
    implementation(libs.ui.tooling.preview)
    implementation(libs.ui.tooling)

    implementation(libs.lifecycleRuntimeCompose)

    // Wear OS
    implementation(libs.wear.foundation)
    implementation(libs.wear.navigation)
    implementation(libs.wear.material)


//    implementation(libs.tiles)
    implementation(libs.tiles.material)

    implementation(libs.lifecycleRuntimeCompose)
    implementation(libs.lifecycleRuntimeKtx)

    implementation(libs.androidx.activity.compose)
    implementation(libs.accompanist.permissions)

    implementation(libs.guava)

    // Lifecycle
    implementation(libs.concurrentFuturesKtx)
    implementation(libs.lifecycleViewmodelKtx)


//    implementation(libs.watchfaceComplicationsDataSourceKtx)
    implementation(libs.navigationRuntimeKtx)


    // Horologist
    implementation(libs.horologist.composables)
    implementation(libs.horologist.compose.tools)
    implementation(libs.horologist.tiles)
    implementation(libs.horologist.health.composables)
    implementation(libs.horologist.health.service)


    // Material
    implementation(libs.compose.material.base)
    implementation(libs.material.icons.extended)

    // Health Services
    implementation(libs.health.services.client)
    implementation(libs.wear.ongoing)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigationCompose)
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

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}