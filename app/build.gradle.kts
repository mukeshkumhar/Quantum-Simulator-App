plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.quantumsimulator"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.quantumsimulator"
        minSdk = 29
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
    buildFeatures{
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // Gson Converter (for JSON parsing)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // okHttp Interceptor
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

//    implementation("com.github.Thereisnospon:CodeView-android:1.2.1")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation(platform("io.github.Rosemoe.sora-editor:bom:0.23.5"))
    implementation("io.github.Rosemoe.sora-editor:editor")
    implementation("io.github.Rosemoe.sora-editor:language-textmate")
//    implementation ("io.github.Rosemoe.sora-editor:language-python:0.23.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
//    kapt ("com.github.bumptech.glide:compiler:4.16.0")



//    codeview
//    implementation ("io.github.kbiakov:CodeView-Android:1.3.2")




}