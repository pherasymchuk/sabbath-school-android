plugins {
    alias(libs.plugins.foundry.base)
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
}

foundry {
    features { compose() }
}

dependencies {
    implementation(libs.google.hilt.android)
    implementation(libs.timber)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(projects.common.designCompose)
    implementation(projects.libraries.blockKit.model)
    implementation(projects.libraries.cascadeCompose)
    implementation(projects.libraries.foundation.android)
    implementation(projects.libraries.navigation3.api)
    implementation(projects.libraries.navigation3.impl)

    ksp(libs.google.hilt.compiler)
}
