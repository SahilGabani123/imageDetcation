plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.customlibrary.imagedetaction"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    // ============== Glide ==============
    implementation (libs.glide)

    // ============== FFMpeg ==============
    implementation (libs.mobile.ffmpeg.full)
    api(libs.media3.exoplayer)
    api(libs.media3.ui)
    api(libs.media3.exoplayer.hls)
    implementation(libs.media3.datasource.cronet)

    // ============== intuit for different devices size ==============
    implementation (libs.sdp.android)
    implementation (libs.ssp.android)

    // ============= Color Picker ===========
    implementation(libs.colorseekbar)

    // ==============Logging==============
    implementation(libs.timber)

    // ==============RxJava==============
    implementation(libs.rxandroid)
    implementation(libs.rxjava)


    // ============= Photo Crop ===========
    implementation(libs.ucrop)
}


publishing {
    publications {
        register<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
                groupId = "com.github.sahil"
                artifactId = "imageDetection"
                version = "1.0.5"
            }
        }
    }
}