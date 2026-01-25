import java.util.Properties
import java.io.FileInputStream
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}
android {
    namespace = "com.example.tradeflow"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.tradeflow"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val apiKey = localProperties.getProperty("GEMINI_API_KEY") ?: ""
        buildConfigField("String", "GEMINI_API_KEY", "\"$apiKey\"")

        val apiKey1 = localProperties.getProperty("GEMINI_API_KEY_1") ?: ""
        val apiKey2 = localProperties.getProperty("GEMINI_API_KEY_2") ?: ""
        val apiKey3 = localProperties.getProperty("GEMINI_API_KEY_3") ?: ""
        val apiKey4 = localProperties.getProperty("GEMINI_API_KEY_4") ?: ""

        buildConfigField("String[]", "GEMINI_API_KEYS", "new String[]{\"${apiKey1}\", \"${apiKey2}\", \"${apiKey3}\", \"${apiKey4}\"}")
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
        buildConfig = true
    }
}

dependencies {
    // Coil
    implementation("io.coil-kt:coil-compose:2.5.0")

    // UI Components
    implementation("com.hbb20:ccp:2.7.3")
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-ui:1.2.0")
    implementation("com.cloudinary:cloudinary-android:2.1.0")
    implementation("com.google.accompanist:accompanist-pager:0.32.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.32.0")

    // Firebase
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.storage.ktx)

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material.icons.extended)

    // Maps
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.maps.android:maps-compose:4.3.3")

    // Theme & DataStore Dependencies
    implementation("androidx.datastore:datastore-preferences:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.8")

    // gemenai
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

}