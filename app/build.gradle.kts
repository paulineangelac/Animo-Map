import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Public Mapbox token (pk.…) lives in local.properties, which is git-ignored:
//   MAPBOX_ACCESS_TOKEN=pk.your_token_here
val mapboxToken: String = Properties().let { props ->
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { props.load(it) }
    props.getProperty("MAPBOX_ACCESS_TOKEN", "")
}

android {
    namespace = "com.itismob.s02.group5.animomap"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.itismob.s02.group5.animomap"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // The Mapbox SDK reads this string resource automatically.
        resValue("string", "mapbox_access_token", mapboxToken)
    }
    buildFeatures {
        resValues = true
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation(libs.mapbox.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
