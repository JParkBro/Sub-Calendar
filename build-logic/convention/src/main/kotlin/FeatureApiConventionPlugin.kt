import jpark.bro.subcal.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** feature:*:api — @Serializable NavKey만 둔다. */
class FeatureApiConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("subcal.android.library")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

            dependencies {
                add("api", libs.findLibrary("androidx-navigation3-runtime").get())
                add("implementation", libs.findLibrary("kotlinx-serialization-json").get())
            }
        }
    }
}
