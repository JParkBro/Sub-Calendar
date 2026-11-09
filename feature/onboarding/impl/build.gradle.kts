plugins {
    alias(libs.plugins.subcal.feature.impl)
}

android {
    namespace = "jpark.bro.subcal.feature.onboarding.impl"
}

dependencies {
    implementation(projects.feature.onboarding.api)
}
