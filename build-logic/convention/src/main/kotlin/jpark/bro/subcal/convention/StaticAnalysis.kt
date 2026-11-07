package jpark.bro.subcal.convention

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension

/**
 * 모든 모듈에 ktlint(포맷) · detekt(정적 분석)를 적용한다.
 * - ktlint 규칙은 루트 .editorconfig, detekt 규칙은 config/detekt/detekt.yml
 * - 모듈에 ktlint-baseline.xml · detekt-baseline.xml이 있으면 기존 위반은 제외한다 (새 코드에는 쓰지 않는다).
 */
internal fun Project.configureStaticAnalysis() {
    pluginManager.apply("org.jlleitschuh.gradle.ktlint")
    pluginManager.apply("io.gitlab.arturbosch.detekt")

    extensions.configure<KtlintExtension> {
        version.set(libs.version("ktlint"))
        baseline.set(file("ktlint-baseline.xml"))
        filter {
            exclude { it.file.path.contains("${java.io.File.separator}build${java.io.File.separator}") }
        }
    }

    extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        baseline = file("detekt-baseline.xml")
    }
}
