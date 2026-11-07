import com.android.build.api.dsl.ApplicationExtension
import jpark.bro.subcal.convention.addUnitTestDependencies
import jpark.bro.subcal.convention.configureApplicationBuildTypes
import jpark.bro.subcal.convention.configureFlavors
import jpark.bro.subcal.convention.configureKotlinAndroid
import jpark.bro.subcal.convention.configureStaticAnalysis
import jpark.bro.subcal.convention.libs
import jpark.bro.subcal.convention.version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")

            extensions.configure<ApplicationExtension> {
                defaultConfig {
                    applicationId = libs.version("projectApplicationId")
                    targetSdk = libs.version("projectTargetSdkVersion").toInt()
                    versionCode = libs.version("projectVersionCode").toInt()
                    versionName = libs.version("projectVersionName")
                }

                buildFeatures {
                    buildConfig = true
                }

                configureKotlinAndroid(this)
                configureFlavors()
                configureApplicationBuildTypes(this)
            }

            addUnitTestDependencies()
            configureStaticAnalysis()
        }
    }
}
