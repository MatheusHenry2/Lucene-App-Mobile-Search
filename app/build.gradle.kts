plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.lucene"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.lucene"
        minSdk = 29
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    // -- Dependências Android (exemplo) --
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    // ...

    // Testes, etc.
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

//    // >>> Lucene (versão 9.12.1) <<<
//    // 'lucene-core' já contém a parte org.apache.lucene.store.*
//    implementation("org.apache.lucene:lucene-core:9.12.1")
//
//    // Para analisadores padrão (antes era 'lucene-analyzers-common')
//    implementation("org.apache.lucene:lucene-analysis-common:9.12.1")
//
//    // QueryParser (MultiFieldQueryParser, etc.)
//    implementation("org.apache.lucene:lucene-queryparser:9.12.1")

    implementation("org.apache.lucene:lucene-core:8.11.2")
    implementation("org.apache.lucene:lucene-analyzers-common:8.11.2")
    implementation("org.apache.lucene:lucene-queryparser:8.11.2")
}