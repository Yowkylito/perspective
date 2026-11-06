import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

/**
 * Release signing, read from outside the repository and never from inside it.
 *
 * Four values, taken from `keystore.properties` at the repository root if it
 * is there, and otherwise from the environment — so a machine uses the file
 * and a build server uses its own secret store, with no second code path.
 * `keystore.properties` is gitignored beside `*.jks` and `*.keystore`, which
 * were already ignored; a password in a tracked file is a password published
 * the moment the repository is.
 *
 * **Absent, the build still works and says why.** Signing is configured only
 * when all four values are present AND the keystore is really on disk, so a
 * clone with no secrets builds debug normally and produces an unsigned
 * release rather than failing to configure. The warning below is there
 * because the alternative is finding out at `adb install`.
 *
 * See the Signing section in CLAUDE.md for the keys and how to make the key.
 */
private val signingProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.isFile) file.inputStream().use(::load)
}

fun signingValue(key: String, variable: String): String? =
    (signingProperties.getProperty(key) ?: System.getenv(variable))
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

val releaseStorePassword = signingValue("storePassword", "PERSPECTIVE_STORE_PASSWORD")
val releaseKeyAlias = signingValue("keyAlias", "PERSPECTIVE_KEY_ALIAS")
val releaseKeyPassword = signingValue("keyPassword", "PERSPECTIVE_KEY_PASSWORD")
val releaseKeystore = signingValue("storeFile", "PERSPECTIVE_STORE_FILE")
    ?.let(rootProject::file)
    ?.takeIf { it.isFile }

val releaseSigningReady = releaseKeystore != null &&
    releaseStorePassword != null &&
    releaseKeyAlias != null &&
    releaseKeyPassword != null

if (!releaseSigningReady &&
    gradle.startParameter.taskNames.any { it.contains("release", ignoreCase = true) }
) {
    logger.warn(
        "Perspective: release signing is not configured, so the release artifact will be " +
            "UNSIGNED — Android will not install it and Play will not accept it. " +
            "See the Signing section in CLAUDE.md.",
    )
}

android {
    namespace = "com.perspective.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.perspective.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 10
        versionName = "0.1.9"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (releaseSigningReady) {
            create("release") {
                storeFile = releaseKeystore
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            // Null when the config above was not created, which is what
            // leaves the artifact unsigned rather than failing the build.
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            // Shared-element transitions are still experimental on Compose
            // animation 1.9.3 (the version compose-bom 2025.10.00 pins). They
            // stabilise in 1.10.0; drop this when the BOM moves past it.
            freeCompilerArgs.add("-opt-in=androidx.compose.animation.ExperimentalSharedTransitionApi")
        }
    }
    buildFeatures { compose = true }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.androidx.core.splashscreen)

    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}
