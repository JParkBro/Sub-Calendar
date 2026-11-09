plugins {
    alias(libs.plugins.subcal.android.library)
    alias(libs.plugins.subcal.room)
    alias(libs.plugins.subcal.koin)
}

android {
    namespace = "jpark.bro.subcal.core.database"
}

dependencies {
    // DAO 테스트: Robolectric + in-memory Room (TESTING.md 2-1)
    testImplementation(libs.bundles.robolectric)
}
