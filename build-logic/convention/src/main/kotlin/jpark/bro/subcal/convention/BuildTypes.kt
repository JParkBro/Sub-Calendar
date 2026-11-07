package jpark.bro.subcal.convention

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Project
import java.util.Properties

/**
 * release 서명은 루트의 keystore.properties(커밋 금지)에서 읽는다.
 * 파일이 없으면 서명 없이 빌드한다 (CI · 다른 개발자 환경).
 */
internal fun Project.configureApplicationBuildTypes(applicationExtension: ApplicationExtension) {
    val keystoreProperties = Properties().apply {
        rootProject.file("keystore.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
    }

    applicationExtension.apply {
        if (!keystoreProperties.isEmpty) {
            signingConfigs.register("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }

        buildTypes {
            getByName("release") {
                isMinifyEnabled = true
                isShrinkResources = true
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                if (!keystoreProperties.isEmpty) {
                    signingConfig = signingConfigs.getByName("release")
                }
            }
        }
    }
}
