import jpark.bro.subcal.convention.addUnitTestDependencies
import jpark.bro.subcal.convention.configureKotlinJvm
import jpark.bro.subcal.convention.configureStaticAnalysis
import org.gradle.api.Plugin
import org.gradle.api.Project

class JvmLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")

            configureKotlinJvm()
            addUnitTestDependencies()
            configureStaticAnalysis()
        }
    }
}
