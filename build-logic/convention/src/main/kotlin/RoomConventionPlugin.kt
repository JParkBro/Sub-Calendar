import androidx.room.gradle.RoomExtension
import com.google.devtools.ksp.gradle.KspExtension
import jpark.bro.subcal.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class RoomConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("androidx.room")
            pluginManager.apply("com.google.devtools.ksp")

            extensions.configure<KspExtension> {
                arg("room.generateKotlin", "true")
            }

            // 스키마 JSON은 커밋하고 마이그레이션 테스트에서 사용한다.
            extensions.configure<RoomExtension> {
                schemaDirectory("$projectDir/schemas")
            }

            dependencies {
                add("implementation", libs.findBundle("room").get())
                add("ksp", libs.findLibrary("room-compiler").get())
                add("androidTestImplementation", libs.findLibrary("room-testing").get())
            }
        }
    }
}
