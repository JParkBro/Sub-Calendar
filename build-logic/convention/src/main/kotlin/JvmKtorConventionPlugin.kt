import jpark.bro.subcal.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** Ktor 클라이언트(OkHttp 엔진) + kotlinx.serialization. JVM · Android 모듈 모두에 적용할 수 있다. */
class JvmKtorConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

            dependencies {
                add("implementation", libs.findBundle("ktor").get())
                add("implementation", libs.findLibrary("kotlinx-serialization-json").get())
                add("testImplementation", libs.findLibrary("ktor-client-mock").get())
            }
        }
    }
}
