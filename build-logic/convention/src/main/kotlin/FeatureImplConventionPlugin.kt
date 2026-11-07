import jpark.bro.subcal.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** feature:*:impl — 화면 · ViewModel · Koin 모듈. 다른 기능은 api만 직접 추가한다. */
class FeatureImplConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("subcal.android.library")
            pluginManager.apply("subcal.android.compose")
            pluginManager.apply("subcal.koin")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

            dependencies {
                add("implementation", project(":core:ui"))
                add("implementation", project(":core:designsystem"))
                add("implementation", project(":core:navigation"))
                add("implementation", project(":core:data"))
                add("implementation", project(":core:domain"))
                add("implementation", project(":core:model"))
                add("testImplementation", project(":core:testing"))

                add("implementation", libs.findBundle("navigation3").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                add("implementation", libs.findBundle("koin-compose").get())
            }
        }
    }
}
