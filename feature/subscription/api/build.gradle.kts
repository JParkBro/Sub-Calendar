plugins {
    alias(libs.plugins.subcal.feature.api)
}

android {
    namespace = "jpark.bro.subcal.feature.subscription.api"
}

dependencies {
    api(projects.core.model)
}
