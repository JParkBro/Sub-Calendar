package jpark.bro.subcal.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.addComposeDependencies() {
    dependencies {
        val bom = libs.findLibrary("androidx-compose-bom").get()
        add("implementation", platform(bom))
        add("androidTestImplementation", platform(bom))

        add("implementation", libs.findBundle("compose").get())
        add("debugImplementation", libs.findBundle("compose-debug").get())
        add("androidTestImplementation", libs.findLibrary("androidx-compose-ui-test-junit4").get())
    }
}
