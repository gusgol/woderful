plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlin.serialization) apply(false)
    alias(libs.plugins.org.jetbrains.kotlin.kapt) apply false
    alias(libs.plugins.hilt) apply(false)
}