plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("jacoco")
}

android {
    namespace = "com.example.libretta"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.libretta"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.example.libretta.test.TestRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "SKIP_AUTH_API", "true")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "SKIP_AUTH_API", "false")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        animationsDisabled = true
    }

    lint {
        warningsAsErrors = true
        abortOnError = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

tasks.withType<Test> {
    extensions.configure(JacocoTaskExtension::class) {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        html.required.set(true)
        xml.required.set(true)
    }

    val mainSrc = "$projectDir/src/main/java"
    val debugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(
            "**/R.class",
            "**/R\$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            // UI層 (Instrumented テストでカバー)
            "**/*Activity.*",
            "**/debug/**",
        )
    }
    val execData = fileTree(layout.buildDirectory) {
        include("jacoco/testDebugUnitTest.exec")
    }

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(files(execData))
}

dependencies {
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    // HTTP client
    implementation("com.squareup.okhttp3:okhttp:5.3.2")

    // ViewModel + Coroutines
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("androidx.activity:activity-ktx:1.13.0")

    // Cucumber (JVM unit tests)
    testImplementation("io.cucumber:cucumber-java:7.34.3")
    testImplementation("io.cucumber:cucumber-junit:7.34.3")
    testImplementation("junit:junit:4.13.2")

    // API testing
    testImplementation("com.squareup.okhttp3:mockwebserver3:5.3.2")
    testImplementation("org.json:json:20251224")

    // Android instrumented tests
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.7.0")
    androidTestImplementation("com.squareup.okhttp3:mockwebserver3:5.3.2")
    androidTestImplementation("io.cucumber:cucumber-android:7.18.1")
}
