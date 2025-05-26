plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)

}

android {
    namespace = "com.thuhang.hethongnuoica"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.thuhang.hethongnuoica"
        minSdk = 23
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("com.airbnb.android:lottie:6.3.0")
    implementation ("com.github.yalantis:ucrop:2.2.8")





}