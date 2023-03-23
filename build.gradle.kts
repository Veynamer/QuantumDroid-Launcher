import com.android.build.gradle.internal.tasks.factory.dependsOn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:7.4.2")
    }
}
val vKotlin = "1.8.10"
val vCompose = "1.3.1"
val vComposeCompiler = "1.4.3"
val vAccompanist = "0.27.0"
val vRoom = "2.5.0-beta01"

plugins {
    id("com.android.application").version("7.4.2")
    kotlin("android").version("1.8.10")
    kotlin("plugin.parcelize").version("1.8.10")
    kotlin("plugin.serialization").version("1.8.10")
    id("com.google.devtools.ksp").version("1.8.10-1.0.9")
    id("com.google.protobuf").version("0.9.1")
}

allprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
        }
    }
}

val prebuiltsDir: String = "prebuilts/"
android {
    namespace = "com.android.launcher3"
    compileSdk = 33

    val name = "1.0"
    val code = 950

    defaultConfig {
        minSdk = 26
        targetSdk = 33
        applicationId = "com.saggitt.omega"

        versionName = name
        versionCode = code

        buildConfigField("String", "BUILD_DATE", "\"${getBuildDate()}\"")
        buildConfigField("boolean", "ENABLE_AUTO_INSTALLS_LAYOUT", "false")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    applicationVariants.all { variant ->
        variant.outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "NeoLauncher_v${variant.versionName}_build_${variant.versionCode}.apk"
        }
        variant.resValue(
            "string",
            "launcher_component",
            "${variant.applicationId}/com.saggitt.omega.OmegaLauncher"
        )
        true
    }

    buildTypes {
        named("debug") {
            isMinifyEnabled = false
            applicationIdSuffix = ".alpha"
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher_debug"
            manifestPlaceholders["appIconRound"] = "@mipmap/ic_launcher_round_debug"
            signingConfig = signingConfigs.getByName("debug")
        }
        create("neo") {
            isMinifyEnabled = false
            applicationIdSuffix = ".neo"
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher_debug"
            manifestPlaceholders["appIconRound"] = "@mipmap/ic_launcher_round_debug"
        }

        named("release") {
            isMinifyEnabled = false
            setProguardFiles(listOf("proguard-android-optimize.txt", "proguard.flags"))
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher"
            manifestPlaceholders["appIconRound"] = "@mipmap/ic_launcher_round"
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        create("primary") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildFeatures {
        compose = true
        dataBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = vComposeCompiler
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = compileOptions.sourceCompatibility.toString()
    }

    packagingOptions {
        jniLibs {
            pickFirsts += listOf("**/libeasyBypass.so")
        }
        resources.excludes.add("META-INF/LICENSE.md")
        resources.excludes.add("META-INF/LICENSE-notice.md")
    }

    flavorDimensionList.clear()
    flavorDimensionList.addAll(listOf("app", "custom"))

    productFlavors {
        create("aosp") {
            dimension = "app"
            applicationId = "com.saggitt.omega"
            testApplicationId = "com.android.launcher3.tests"
        }

        create("omega") {
            dimension = "custom"
        }
    }

    sourceSets {
        named("main") {
            res.srcDirs(listOf("res"))
            java.srcDirs(listOf("src", "src_plugins", "src_ui_overrides"))
            assets.srcDirs(listOf("assets"))
            manifest.srcFile("AndroidManifest-common.xml")
        }

        /*named("androidTest") {
            res.srcDirs(listOf("tests/res"))
            java.srcDirs(listOf("tests/src", "tests/tapl"))
            manifest.srcFile("tests/AndroidManifest-common.xml")
        }

        named("androidTestDebug") {
            java.srcDirs("tests/src_common")
            manifest.srcFile("tests/AndroidManifest.xml")
        }*/

        named("aosp") {
            java.srcDirs(listOf("src_flags", "src_shortcuts_overrides"))
        }

        named("omega") {
            res.srcDirs(listOf("Omega/res"))
            java.srcDirs(listOf("Omega/src", "Omega/src_overrides"))
            aidl.srcDirs(listOf("Omega/aidl"))
            manifest.srcFile("Omega/AndroidManifest.xml")
        }

        protobuf {
            // Configure the protoc executable
            protoc {
                artifact = "com.google.protobuf:protoc:3.21.12"
            }
            generateProtoTasks {
                all().forEach { task ->
                    task.builtins {
                        create("java") {
                            option("lite")
                        }
                    }
                }
            }
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
        disable += listOf("MissingTranslation", "ExtraTranslation")
    }
}

dependencies {
    implementation(project(":iconloaderlib"))
    implementation(kotlin("stdlib", vKotlin))

    //UI
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.dynamicanimation:dynamicanimation:1.1.0-alpha03")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    implementation("com.google.android.material:material:1.9.0-alpha02")

    //Libs
    implementation("com.google.protobuf:protobuf-javalite:3.21.12")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation("com.github.ChickenHook:RestrictionBypass:2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.10")
    implementation("com.github.samanzamani:PersianDate:1.5.4")
    implementation("com.github.KwabenBerko:OpenWeatherMap-Android-Library:2.0.1") {
        exclude("com.android.support", "support-compat")
        exclude("com.android.support", "appcompat-v7")
    }
    implementation("com.raedapps:alwan:1.0.0")
    implementation("io.github.hokofly:hoko-blur:1.3.7")

    //Compose
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.compose.compiler:compiler:$vComposeCompiler")
    implementation("androidx.compose.runtime:runtime:$vCompose")
    implementation("androidx.compose.ui:ui:$vCompose")
    implementation("androidx.compose.ui:ui-tooling:$vCompose")
    implementation("androidx.compose.ui:ui-tooling-preview:$vCompose")
    implementation("androidx.compose.foundation:foundation:$vCompose")
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("io.github.fornewid:material-motion-compose-core:0.9.0")
    implementation("io.coil-kt:coil-compose:2.2.2")
    implementation("com.google.android.material:compose-theme-adapter-3:1.1.1")

    implementation("androidx.compose.material:material:$vCompose")
    implementation("androidx.compose.material3:material3:1.0.1")

    //Accompanist
    implementation("com.google.accompanist:accompanist-flowlayout:$vAccompanist")
    implementation("com.google.accompanist:accompanist-insets-ui:$vAccompanist")
    implementation("com.google.accompanist:accompanist-navigation-animation:$vAccompanist")
    implementation("com.google.accompanist:accompanist-systemuicontroller:$vAccompanist")
    implementation("com.google.accompanist:accompanist-drawablepainter:$vAccompanist")
    implementation("com.google.accompanist:accompanist-pager:$vAccompanist")
    implementation("com.google.accompanist:accompanist-pager-indicators:$vAccompanist")

    //Room
    implementation("androidx.room:room-runtime:$vRoom")
    implementation("androidx.room:room-ktx:$vRoom")
    ksp("androidx.room:room-compiler:$vRoom")

    //DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Jars
    implementation(fileTree(baseDir = "${prebuiltsDir}/libs").include("SystemUI-statsd.jar"))
    implementation(fileTree(baseDir = "${prebuiltsDir}/libs").include("WindowManager-Shell.jar"))

    api("com.airbnb.android:lottie:3.3.0")

    protobuf(files("protos/"))
    protobuf(files("protos_overrides/"))

    //Test
    testImplementation("junit:junit:4.13.2")
    implementation("junit:junit:4.13.2")
    androidTestImplementation("org.junit.jupiter:junit-jupiter:5.9.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")

    androidTestImplementation("com.google.truth:truth:0.40")
    androidTestImplementation("org.mockito:mockito-core:5.0.0")
    androidTestImplementation("com.google.dexmaker:dexmaker:1.2")
    androidTestImplementation("com.google.dexmaker:dexmaker-mockito:1.2")
    androidTestImplementation("androidx.annotation:annotation:1.6.0")
    androidTestImplementation("com.android.support.test:runner:1.0.2")
    androidTestImplementation("com.android.support.test:rules:1.0.2")
    androidTestImplementation("com.android.support.test.uiautomator:uiautomator-v18:2.1.3")
}

// using a task as a preBuild dependency instead of a function that takes some time insures that it runs
task("detectAndroidLocals") {
    val langsList: MutableSet<String> = HashSet()

    // in /res are (almost) all languages that have a translated string is saved. this is safer and saves some time
    fileTree("res").visit {
        if (this.file.path.endsWith("strings.xml")
            && this.file.canonicalFile.readText().contains("<string")
        ) {
            var languageCode = this.file.parentFile.name.replace("values-", "")
            languageCode = if (languageCode == "values") "en" else languageCode
            langsList.add(languageCode)
        }
    }
    val langsListString = "{${langsList.joinToString(",") { "\"${it}\"" }}}"
    android.defaultConfig.buildConfigField(
        "String[]",
        "DETECTED_ANDROID_LOCALES",
        langsListString
    )
}
tasks.preBuild.dependsOn("detectAndroidLocals")

@SuppressWarnings(
    "UnnecessaryQualifiedReference",
    "SpellCheckingInspection",
    "GroovyUnusedDeclaration"
)
// Returns the build date in a RFC3339 compatible format. TZ is always converted to UTC
fun getBuildDate(): String {
    val RFC3339_LIKE = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    RFC3339_LIKE.timeZone = TimeZone.getTimeZone("UTC")
    return RFC3339_LIKE.format(Date())
}