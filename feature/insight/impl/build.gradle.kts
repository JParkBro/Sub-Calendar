plugins {
    alias(libs.plugins.subcal.feature.impl)
}

android {
    namespace = "jpark.bro.subcal.feature.insight.impl"
}

dependencies {
    implementation(projects.feature.insight.api)
    implementation(projects.feature.subscription.api)
}
