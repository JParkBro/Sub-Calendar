plugins {
    alias(libs.plugins.subcal.android.library)
    alias(libs.plugins.subcal.koin)
}

android {
    namespace = "jpark.bro.subcal.core.datastore"
}

dependencies {
    api(projects.core.model)
    implementation(libs.androidx.datastore.preferences)
}
