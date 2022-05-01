plugins {
    id("com.android.library")
    id("maven-publish")
    id("kotlinx-serialization")
    kotlin("android")
    kotlin("kapt")
}

repositories {
    mavenCentral()
}

publishing {
    publications {
        create("maven_public",MavenPublication::class) {
            groupId = "com.github.sieunju"
            artifactId = "httptracking"
            version = Apps.versionName
        }
    }
}

android {
    compileSdk = Apps.compileSdkVersion

    // ktlint
    lint {
        abortOnError = false
    }

    defaultConfig {
        minSdk = Apps.minSdkVersion
        targetSdk = Apps.targetSdkVersion
         consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
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
        dataBinding = true
    }
    kapt {
        correctErrorTypes = true
    }
}

dependencies {
    /**
     * Android X
     */
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")

    /**
     * Rx
     */
    implementation("io.reactivex.rxjava3:rxjava:3.1.2")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.0")
    implementation("io.reactivex.rxjava3:rxkotlin:3.0.1")

    /**
     * Network
     */
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    /**
     * Kotlinx Serialization
     */
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}