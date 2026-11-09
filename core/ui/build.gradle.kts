plugins {
    alias(libs.plugins.subcal.android.library)
    alias(libs.plugins.subcal.android.compose)
}

android {
    namespace = "jpark.bro.subcal.core.ui"
}

dependencies {
    api(libs.androidx.lifecycle.viewmodel)
    // MoneyText · PaymentRow의 공개 타입
    api(projects.core.model)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(projects.core.designsystem)
}
