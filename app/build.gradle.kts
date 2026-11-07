plugins {
    alias(libs.plugins.subcal.android.application)
    alias(libs.plugins.subcal.android.compose)
    alias(libs.plugins.subcal.koin)
}

android {
    namespace = "jpark.bro.subcal"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.bundles.navigation3)
    implementation(libs.bundles.koin.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
