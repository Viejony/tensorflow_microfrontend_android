import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.vanniktech.maven.publish") version "0.32.0"
}

android {
    namespace = "io.github.viejony"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        externalNativeBuild {
            cmake {
                cppFlags("")
            }
        }
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
    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

group = "io.github.viejony"
version = "0.1.0"

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(group.toString(), "microfrontend", version.toString())

    pom {
        name = "Tensorflow Audio Microfrontend"
        description = "Tensorflow Audio Microfrontend: Library for Android Application"
        inceptionYear = "2025"
        url = "https://github.com/Viejony/tensorflow_microfrontend_android"
        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
            }
        }
        developers {
            developer {
                id = "Viejony"
                name = "Jhonyfer Angarita Moreno"
                url = "https://github.com/Viejony"
            }
        }
        scm {
            connection = "scm:git:git://github.com/Viejony/tensorflow_microfrontend_android.git"
            developerConnection = "scm:git:ssh://git@github.com/Viejony/tensorflow_microfrontend_android.git"
            url = "https://github.com/Viejony/tensorflow_microfrontend_android"
        }
    }
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}