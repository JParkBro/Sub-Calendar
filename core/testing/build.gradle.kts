plugins {
    alias(libs.plugins.subcal.android.library)
}

android {
    namespace = "jpark.bro.subcal.core.testing"
}

dependencies {
    api(projects.core.data)
    implementation(libs.junit)
    implementation(libs.kotlinx.coroutines.test)
}
