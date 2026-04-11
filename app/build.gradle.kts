plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.yurikh.yourant"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.yurikh.yourant"
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = "0.1.2-alpha"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true // For desugar
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    implementation(libs.work.runtime)
    implementation(libs.viewpager2)
    implementation(libs.legacy.support.v4)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.okhttp)
    implementation(libs.flexbox)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.rome)
}