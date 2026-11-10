plugins {
    alias(libs.plugins.subcal.feature.impl)
}

android {
    namespace = "jpark.bro.subcal.feature.subscription.impl"
}

dependencies {
    implementation(projects.feature.subscription.api)
    implementation(projects.feature.party.api)
    // 첫 저장 때 알림 권한 요청 (product-spec 5장)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
}
