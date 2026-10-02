import java.util.Properties
import java.io.FileInputStream

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.priti.dailykit"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.priti.dailykit"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePropsFile = rootProject.file("keystore.properties")
      if (keystorePropsFile.exists()) {
        val props = Properties()
        FileInputStream(keystorePropsFile).use { stream ->
          props.load(stream)
        }
        val storeFilePath = props.getProperty("DAILYKIT_KEYSTORE_PATH") ?: "dailykit-upload.jks"
        val resolvedFile = file(storeFilePath)
        val rootResolvedFile = rootProject.file(storeFilePath)
        storeFile = when {
          resolvedFile.exists() -> resolvedFile
          rootResolvedFile.exists() -> rootResolvedFile
          rootProject.file("dailykit-upload.jks").exists() -> rootProject.file("dailykit-upload.jks")
          file("${rootDir}/dailykit-upload.jks").exists() -> file("${rootDir}/dailykit-upload.jks")
          else -> file("${rootDir}/debug.keystore")
        }
        storePassword = props.getProperty("DAILYKIT_STORE_PASSWORD") ?: "android"
        keyAlias = props.getProperty("DAILYKIT_KEY_ALIAS") ?: "androiddebugkey"
        keyPassword = props.getProperty("DAILYKIT_KEY_PASSWORD") ?: "android"
      } else {
        val envStorePath = System.getenv("DAILYKIT_KEYSTORE_PATH") ?: "dailykit-upload.jks"
        val keystoreFile = file(envStorePath)
        val rootKeystoreFile = rootProject.file(envStorePath)
        storeFile = when {
          keystoreFile.exists() -> keystoreFile
          rootKeystoreFile.exists() -> rootKeystoreFile
          rootProject.file("dailykit-upload.jks").exists() -> rootProject.file("dailykit-upload.jks")
          file("${rootDir}/dailykit-upload.jks").exists() -> file("${rootDir}/dailykit-upload.jks")
          else -> file("${rootDir}/debug.keystore")
        }
      }
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures {
    viewBinding = true
    buildConfig = true
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
    }
  }

  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.material)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.androidx.recyclerview)
  implementation(libs.androidx.cardview)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.play.services.ads)

  testImplementation(libs.junit)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.androidx.core)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
}
