plugins {
    alias(libs.plugins.subcal.feature.impl)
}

android {
    namespace = "jpark.bro.subcal.feature.settings.impl"
}

dependencies {
    implementation(projects.feature.settings.api)
    implementation(libs.androidx.core.ktx)
}
