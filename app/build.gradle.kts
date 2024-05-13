plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")


}


android {
    namespace = "com.example.rassoonlineapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.rassoonlineapp"
        minSdk = 21
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}


dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.vectordrawable:vectordrawable:1.1.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation("androidx.media3:media3-common:1.3.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
 //
    //   chrimplementation("androidx.compose.material3:material3-android:1.2.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("com.googlecode.libphonenumber:libphonenumber:8.12.34")

    //picasso library
    implementation ("com.squareup.picasso:picasso:2.71828")

    implementation("com.vanniktech:android-image-cropper:4.5.0")
    implementation ("com.soundcloud.android:android-crop:1.0.1@aar")
    implementation ("com.github.bumptech.glide:glide:4.12.0")

    implementation ("com.stripe:stripe-java:25.0.0")







    // Firebase

    implementation("androidx.browser:browser:1.7.0")
    implementation("com.google.firebase:firebase-analytics:21.5.1")
    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-database:20.3.0")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.google.firebase:firebase-messaging:23.4.1")
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))


    implementation ("com.google.firebase:firebase-auth-ktx")
    implementation ("com.google.android.gms:play-services-auth:20.5.0")

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.5")

    // Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.6.2")
    implementation ("com.squareup.retrofit2:converter-gson:2.6.0")

        //Charts

    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0") // Versão pode variar

    //ViewPager2

    implementation ("androidx.viewpager2:viewpager2:1.0.0-rec01")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.viewpager:viewpager:1.0.0")


    // Stripe Android SDK
    implementation ("com.stripe:stripe-android:20.42.0")

    //PDF GENERATE
    implementation ("com.itextpdf:itextg:5.5.10")


    implementation ("com.firebaseui:firebase-ui-database:7.2.0")


}
