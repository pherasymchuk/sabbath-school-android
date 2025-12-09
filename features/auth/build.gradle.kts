plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.foundry.base)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "app.ss.auth"
}

foundry {
    features { compose() }
    android { features { robolectric() } }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.auth)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.google.hilt.android)
    implementation(libs.google.id)
    implementation(libs.timber)
    implementation(projects.common.auth)
    implementation(projects.common.designCompose)
    implementation(projects.common.models)
    implementation(projects.common.translations)
    implementation(projects.libraries.foundation.coroutines)
    implementation(projects.libraries.navigation3.api)
    implementation(projects.libraries.navigation3.impl)

    testImplementation(libs.bundles.testing.common)
    testImplementation(projects.libraries.foundation.coroutines.test)

    ksp(libs.google.hilt.compiler)
}
