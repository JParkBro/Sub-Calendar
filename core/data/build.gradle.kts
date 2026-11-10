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
    // 지운 구독 정리 Worker
    implementation(libs.androidx.work.runtime)
    implementation(libs.koin.androidx.workmanager)

    // Repository 테스트: Robolectric + in-memory Room (TESTING.md 5-6)
    testImplementation(libs.bundles.robolectric)
    testImplementation(libs.bundles.room)
    testImplementation(libs.androidx.datastore.preferences)
    testImplementation(libs.androidx.work.testing)
}
