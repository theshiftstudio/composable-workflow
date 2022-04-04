import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    kotlin("android")
    // id("org.jetbrains.dokka")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

// apply(from = rootProject.file(".buildscript/configure-maven-publish.gradle"))
// apply(from = rootProject.file(".buildscript/configure-android-defaults.gradle"))
// apply(from = rootProject.file(".buildscript/android-ui-tests.gradle"))

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures.compose = true
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        @Suppress("SuspiciousCollectionReassignment")
        freeCompilerArgs += listOf(
            "-Xopt-in=kotlin.RequiresOptIn"
        )
    }
}

dependencies {
    // api(project(":workflow-core"))
    api(libs.squareup.workflow.core)
    // api(project(":workflow-ui:core-android"))
    api(project(":workflow:workflow-ui:core-android"))
    // api(project(":workflow-ui:container-android"))
    api(project(":workflow:workflow-ui:container-android"))
    api(libs.androidx.compose.foundation)

    implementation(libs.androidx.savedstate)

    androidTestImplementation(libs.squareup.workflow.runtime)
    // androidTestImplementation(project(":workflow-runtime"))
    androidTestImplementation(libs.androidx.activity.core)
    androidTestImplementation(libs.androidx.compose.ui)
    androidTestImplementation(libs.kotlin.test.jdk)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.truth)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
