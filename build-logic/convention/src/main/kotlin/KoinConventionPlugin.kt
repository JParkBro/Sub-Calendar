import jpark.bro.subcal.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KoinConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            dependencies {
                val bom = libs.findLibrary("koin-bom").get()
                add("implementation", platform(bom))
                add("testImplementation", platform(bom))

                add("implementation", libs.findLibrary("koin-android").get())
                add("testImplementation", libs.findLibrary("koin-test-junit4").get())
            }
        }
    }
}
