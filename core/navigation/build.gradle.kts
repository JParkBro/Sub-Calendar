plugins {
    alias(libs.plugins.subcal.android.library)
    alias(libs.plugins.subcal.android.compose)
}

android {
    namespace = "jpark.bro.subcal.core.navigation"
}

dependencies {
    api(libs.androidx.navigation3.runtime)
}
