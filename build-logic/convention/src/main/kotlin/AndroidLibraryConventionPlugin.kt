import com.android.build.api.dsl.LibraryExtension
import jpark.bro.subcal.convention.addUnitTestDependencies
import jpark.bro.subcal.convention.configureFlavors
import jpark.bro.subcal.convention.configureKotlinAndroid
import jpark.bro.subcal.convention.configureStaticAnalysis
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                configureFlavors()
            }

            addUnitTestDependencies()
            configureStaticAnalysis()
        }
    }
}
