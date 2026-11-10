plugins {
    alias(libs.plugins.subcal.feature.impl)
}

android {
    namespace = "jpark.bro.subcal.feature.trial.impl"
}

dependencies {
    implementation(projects.feature.trial.api)
    implementation(projects.feature.subscription.api)
}
