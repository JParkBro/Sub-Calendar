plugins {
    alias(libs.plugins.subcal.android.application)
    alias(libs.plugins.subcal.android.compose)
    alias(libs.plugins.subcal.koin)
}

android {
    namespace = "jpark.bro.subcal"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.bundles.navigation3)
    implementation(libs.bundles.koin.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.koin.androidx.workmanager)

    // core
    implementation(projects.core.navigation)
    implementation(projects.core.designsystem)
    implementation(projects.core.model)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.data)
    implementation(projects.core.notification)

    // feature
    implementation(projects.feature.onboarding.api)
    implementation(projects.feature.onboarding.impl)
    implementation(projects.feature.home.api)
    implementation(projects.feature.home.impl)
    implementation(projects.feature.subscription.api)
    implementation(projects.feature.subscription.impl)
    implementation(projects.feature.calendar.api)
    implementation(projects.feature.calendar.impl)
    implementation(projects.feature.settings.api)
    implementation(projects.feature.settings.impl)

    testImplementation(projects.core.testing)
}
