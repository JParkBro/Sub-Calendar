plugins {
    alias(libs.plugins.subcal.feature.impl)
}

android {
    namespace = "jpark.bro.subcal.feature.settings.impl"
}

dependencies {
    implementation(projects.feature.settings.api)
    implementation(projects.feature.trial.api)
    implementation(projects.feature.party.api)
    implementation(libs.androidx.core.ktx)
    // 백업 파일 저장 · 선택 창 (SAF)
    implementation(libs.androidx.activity.compose)
}
