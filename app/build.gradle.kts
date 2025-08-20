import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
}

// ===== CARREGANDO SECRETS.PROPERTIES =====
val secretsPropertiesFile = rootProject.file("secrets.properties")
val secretsProperties = Properties()

if (secretsPropertiesFile.exists()) {
    secretsProperties.load(FileInputStream(secretsPropertiesFile))
    println("✅ secrets.properties carregado com sucesso!")
} else {
    println("Arquivo secrets.properties não encontrado!")
    println("Crie o arquivo na raiz do projeto com o conteúdo:")
    println("   MAPS_API_KEY=sua_chave_aqui")
}

android {
    namespace = "com.example.safemode"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.safemode"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ===== CONFIGURANDO A API KEY =====
        val mapsApiKey = secretsProperties.getProperty("MAPS_API_KEY", "")

        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")

        manifestPlaceholders["mapsApiKey"] = mapsApiKey

        if (mapsApiKey.isEmpty()) {
            println("MAPS_API_KEY não encontrada em secrets.properties!")
        } else {
            println("MAPS_API_KEY configurada: ${mapsApiKey.take(10)}...")
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.google.android.gms:play-services-maps:19.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.maps.android:android-maps-utils:3.14.0")
}