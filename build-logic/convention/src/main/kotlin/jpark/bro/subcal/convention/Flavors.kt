package jpark.bro.subcal.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension

/**
 * 빌드 변형: dev(개발 서버 · 에뮬레이터) / prod(실서버).
 * 모든 Android 모듈이 같은 차원 · 이름을 가져야 app과 라이브러리의 변형이 맞물린다.
 */
internal enum class AppFlavor(val flavorName: String, val applicationIdSuffix: String?) {
    Dev("dev", ".dev"),
    Prod("prod", null),
}

private const val FLAVOR_DIMENSION = "env"

internal fun ApplicationExtension.configureFlavors() {
    flavorDimensions += FLAVOR_DIMENSION
    AppFlavor.entries.forEach { flavor ->
        productFlavors.register(flavor.flavorName) {
            dimension = FLAVOR_DIMENSION
            flavor.applicationIdSuffix?.let { applicationIdSuffix = it }
        }
    }
}

internal fun LibraryExtension.configureFlavors() {
    flavorDimensions += FLAVOR_DIMENSION
    AppFlavor.entries.forEach { flavor ->
        productFlavors.register(flavor.flavorName) {
            dimension = FLAVOR_DIMENSION
        }
    }
}
