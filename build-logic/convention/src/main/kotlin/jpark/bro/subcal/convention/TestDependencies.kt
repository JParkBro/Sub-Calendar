package jpark.bro.subcal.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** 모든 모듈 공통 JVM 테스트 의존성. */
internal fun Project.addUnitTestDependencies() {
    dependencies {
        add("testImplementation", libs.findBundle("test").get())
    }
}
