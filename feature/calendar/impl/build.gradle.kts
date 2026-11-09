plugins {
    alias(libs.plugins.subcal.feature.impl)
}

android {
    namespace = "jpark.bro.subcal.feature.calendar.impl"
}

dependencies {
    implementation(projects.feature.calendar.api)
    implementation(projects.feature.subscription.api)
}
