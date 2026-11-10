enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SubCalendar"
include(":app")

include(":core:model")
include(":core:common")
include(":core:domain")
include(":core:designsystem")
include(":core:ui")
include(":core:database")
include(":core:datastore")
include(":core:data")
include(":core:navigation")
include(":core:testing")
include(":core:notification")

include(":feature:onboarding:api")
include(":feature:onboarding:impl")
include(":feature:home:api")
include(":feature:home:impl")
include(":feature:subscription:api")
include(":feature:subscription:impl")
include(":feature:calendar:api")
include(":feature:calendar:impl")
