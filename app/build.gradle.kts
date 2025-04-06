plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")


}

android {
    namespace = "com.example.weathery"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.weathery"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.androidx.storage)
    implementation(libs.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.core:core-splashscreen:1.0.0")
    implementation ("com.google.code.gson:gson:2.10.1")

    //Scoped API
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose-android:2.8.7")
    //Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")
    //Room
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    // Kotlin Symbol Processing (KSP)
    ksp("androidx.room:room-compiler:$room_version")
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$room_version")
    //Glide
    implementation ("com.github.bumptech.glide:compose:1.0.0-beta01")
    //LiveData & Compose
    val compose_version = "1.0.0"
    implementation ("androidx.compose.runtime:runtime-livedata:$compose_version")


    implementation("androidx.compose.ui:ui:1.7.8")
    implementation("androidx.compose.material:material:1.7.8")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    val nav_version = "2.8.8"
    implementation("androidx.navigation:navigation-compose:$nav_version")

    //Serialization for NavArgs
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.airbnb.android:lottie-compose:6.6.3")

    implementation ("com.google.android.gms:play-services-maps:19.1.0")
    implementation ("com.google.maps.android:maps-compose:2.11.4")
    implementation ("com.google.android.gms:play-services-location:21.3.0")

    implementation ("org.osmdroid:osmdroid-android:6.1.16")

    implementation ("androidx.work:work-runtime-ktx:2.10.0")


    implementation ("androidx.lifecycle:lifecycle-runtime:2.8.7")
    implementation ("androidx.lifecycle:lifecycle-viewmodel:2.8.7")
    implementation ("androidx.savedstate:savedstate:1.2.1")
    implementation("androidx.compose.runtime:runtime:1.7.8")
    implementation ("com.google.accompanist:accompanist-permissions:0.31.5-beta")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("com.google.truth:truth:1.1.3") // Optional - nicer assertions
    testImplementation("io.mockk:mockk:1.13.17") // Mocking library

    // ----------------------------------------
    //  Android Instrumented Testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")

    // Room instrumentation test support
    androidTestImplementation("androidx.room:room-testing:2.6.1")

    // ----------------------------------------
    //  Compose UI Testing (only if you're using Jetpack Compose)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.8")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.8")

    // ----------------------------------------
    //  Hilt Testing (only if using Hilt - remove if not needed)
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.48")










}