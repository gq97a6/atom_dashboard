plugins {
    id("com.android.application") version "8.13.0"
    id("org.jetbrains.kotlin.android") version "2.2.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.21"
    id("com.google.gms.google-services") version "4.4.4"
    id("com.google.firebase.crashlytics") version "3.0.6"
    id("io.sentry.android.gradle") version "5.12.2"
}

android {
    namespace = "com.alteratom"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.alteratom.dashboard"
        minSdk = 26
        targetSdk = 36
        versionCode = 35
        versionName = "4.0.0 | stable"
    }

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    buildTypes {
        getByName("release") {
            // isShrinkResources = true
            // isMinifyEnabled = true
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }

        getByName("debug") {
            // isShrinkResources = true
            // isMinifyEnabled = true
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
            isDebuggable = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    sourceSets {
        getByName("main") {
            res.srcDirs(
                    "src/main/res",
                    file("src/main/java/com/alteratom/dashboard/tile/types").listFiles(),
                    "src/main/res/icons"
            )
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }

    packaging {
        resources {
            excludes += "META-INF/*"
        }
    }
}

dependencies {
    implementation("androidx.compose.runtime:runtime-livedata:1.9.4")

    //DO NOT UPDATE
    implementation("androidx.compose.material:material:1.4.3")
    implementation("androidx.compose.animation:animation:1.4.3")
    implementation("androidx.compose.ui:ui-tooling:1.4.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.activity:activity-ktx:1.11.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.6")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("com.android.billingclient:billing:8.1.0")
    implementation("com.android.support:localbroadcastmanager:28.0.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.20.1")
    implementation("com.squareup.okhttp3:okhttp:5.3.0")
    implementation("org.labcluster.arctextview:arctextview:3.0.0")
    implementation("io.netty:netty-handler:4.1.104.Final")
    implementation("com.hivemq:hivemq-mqtt-client:1.3.10")
    implementation(platform("com.hivemq:hivemq-mqtt-client-websocket:1.3.10"))
    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")
    implementation("me.tankery.lib:circularSeekBar:1.4.2")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
}