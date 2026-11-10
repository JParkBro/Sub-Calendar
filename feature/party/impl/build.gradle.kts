plugins {
    alias(libs.plugins.subcal.feature.impl)
}

android {
    namespace = "jpark.bro.subcal.feature.party.impl"
}

dependencies {
    implementation(projects.feature.party.api)
}
