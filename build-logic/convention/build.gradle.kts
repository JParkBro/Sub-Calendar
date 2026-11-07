import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "jpark.bro.subcal.buildlogic"

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    compileOnly(libs.ktlint.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.subcal.android.application.get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.subcal.android.library.get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = libs.plugins.subcal.android.compose.get().pluginId
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("jvmLibrary") {
            id = libs.plugins.subcal.jvm.library.get().pluginId
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("jvmKtor") {
            id = libs.plugins.subcal.jvm.ktor.get().pluginId
            implementationClass = "JvmKtorConventionPlugin"
        }
        register("koin") {
            id = libs.plugins.subcal.koin.get().pluginId
            implementationClass = "KoinConventionPlugin"
        }
        register("room") {
            id = libs.plugins.subcal.room.get().pluginId
            implementationClass = "RoomConventionPlugin"
        }
        register("featureApi") {
            id = libs.plugins.subcal.feature.api.get().pluginId
            implementationClass = "FeatureApiConventionPlugin"
        }
        register("featureImpl") {
            id = libs.plugins.subcal.feature.impl.get().pluginId
            implementationClass = "FeatureImplConventionPlugin"
        }
    }
}
