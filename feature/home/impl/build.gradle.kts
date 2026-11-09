plugins {
    alias(libs.plugins.subcal.feature.impl)
}

android {
    namespace = "jpark.bro.subcal.feature.home.impl"
}

dependencies {
    implementation(projects.feature.home.api)
}
