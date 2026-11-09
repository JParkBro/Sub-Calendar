plugins {
    alias(libs.plugins.subcal.android.library)
    alias(libs.plugins.subcal.koin)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "jpark.bro.subcal.core.data"
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(libs.kotlinx.serialization.json)

    // Repository 테스트: Robolectric + in-memory Room (TESTING.md 5-6)
    testImplementation(libs.bundles.robolectric)
    testImplementation(libs.bundles.room)
    testImplementation(libs.androidx.datastore.preferences)
}
