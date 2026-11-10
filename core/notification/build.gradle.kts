plugins {
    alias(libs.plugins.subcal.android.library)
    alias(libs.plugins.subcal.koin)
}

android {
    namespace = "jpark.bro.subcal.core.notification"
}

dependencies {
    implementation(projects.core.data)
    // 마지막 발송일은 알림만 쓰는 내부 상태라 Repository를 거치지 않는다
    implementation(projects.core.datastore)
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.core.navigation)
    implementation(projects.core.ui)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.work.runtime)
    implementation(libs.koin.androidx.workmanager)

    // Worker 테스트: Robolectric + WorkManager 테스트 도구 (TESTING.md 5-6)
    testImplementation(projects.core.testing)
    testImplementation(libs.bundles.robolectric)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.androidx.datastore.preferences)
}
