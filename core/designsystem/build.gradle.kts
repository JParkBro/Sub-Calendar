plugins {
    alias(libs.plugins.subcal.android.library)
    alias(libs.plugins.subcal.android.compose)
}

android {
    namespace = "jpark.bro.subcal.core.designsystem"
}

dependencies {
    // AppTheme(mode) · SubCalAmountField의 공개 타입
    api(projects.core.model)

    // ThemeTokensSyncTest: theme-tokens.json 파싱
    testImplementation(libs.kotlinx.serialization.json)
}
