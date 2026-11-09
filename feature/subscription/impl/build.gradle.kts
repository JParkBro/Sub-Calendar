plugins {
    alias(libs.plugins.subcal.feature.impl)
}

android {
    namespace = "jpark.bro.subcal.feature.subscription.impl"
}

dependencies {
    implementation(projects.feature.subscription.api)
}
