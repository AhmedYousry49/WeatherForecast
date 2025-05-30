import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
}
val localProperties = Properties()
localProperties.load(File(rootDir, "local.properties").inputStream())
val Api_Key = localProperties.getProperty("Api_Key")

android {
    namespace = "com.iti.uc3.forecast"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.iti.uc3.forecast"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "Api_Key", "\"${Api_Key}\"")

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
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.preference)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation ("com.google.android.gms:play-services-location:21.3.0")


    implementation ("org.jetbrains.kotlin:kotlin-stdlib:2.1.21")
    implementation("androidx.work:work-runtime-ktx:2.10.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.code.gson:gson:2.13.1")
    implementation ("androidx.media:media:1.7.0")


    implementation ("com.squareup.retrofit2:retrofit:3.0.0")
    implementation ("com.squareup.retrofit2:converter-gson:3.0.0")


    kapt("androidx.room:room-compiler:2.7.1")
    implementation ("androidx.room:room-runtime:2.7.1")
    implementation ("androidx.room:room-ktx:2.7.1")


//ViewModel & livedata
    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.0")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.9.0")

//Coroutine
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")

    // Coroutines test support
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")


    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation ("org.mockito:mockito-inline:5.2.0")
    testImplementation ("io.mockk:mockk:1.14.2")


    // For Mockito with Kotlin testImplementation 'org.mockito.kotlin:mockito-kotlin:4.0.0
// OR use this older version if you have compatibility issues
// testImplementation 'org.mockito.kotlin:mockito-kotlin:3.2.0' // For Coroutines testing testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4'



}