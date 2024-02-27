import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
}

kotlin {

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    js {
        nodejs {
            testTask {
                useMocha {
                    timeout = "5000"
                }
            }
        }
        useCommonJs()
        binaries.executable()
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting
        val wasmJsMain by getting
        val jsMain by getting

        val sharingKtorMain by creating {
            dependsOn(commonMain.get())
        }
        val androidDesktopMain by creating {
            dependsOn(sharingKtorMain)
        }

        androidMain.get().dependsOn(androidDesktopMain)
        desktopMain.dependsOn(androidDesktopMain)

        jsMain.dependsOn(sharingKtorMain)


        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlinx.coroutines.android)

            implementation(libs.androidx.lifecycle.viewmodel.ktx)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)

            implementation("com.garmin.connectiq:ciq-companion-app-sdk:2.0.2@aar")
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
        jsMain.dependencies {
            implementation(libs.ktor.client.js)
            implementation(npm("discord.js", "14"))
        }

        sharingKtorMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.websockets)
        }
        androidDesktopMain.dependencies {
            implementation(libs.ktor.client.cio)
        }


        val androidUnitTest by getting
        val desktopTest by getting
        val jsTest by getting

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        val sharingKtorTest by creating {
            dependsOn(commonTest.get())
            jsTest.dependsOn(this)
        }
        val androidDesktopTest by creating {
            dependsOn(sharingKtorTest)
            androidUnitTest.dependsOn(this)
            desktopTest.dependsOn(this)
        }
    }
}

android {
    namespace = "de.deinberater.tictactoe"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "de.deinberater.tictactoe"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "de.deinberater.tictactoe"
            packageVersion = "1.0.0"
        }
    }
}

compose.experimental {
    web.application {}
}

tasks.named("jsNodeDevelopmentRun") {
    mustRunAfter("jsProductionExecutableCompileSync")
}

tasks.named("jsNodeProductionRun") {
    mustRunAfter("jsDevelopmentExecutableCompileSync")
}

tasks.named("jsNodeRun") {
    mustRunAfter("jsProductionExecutableCompileSync")
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile> {
    kotlinOptions.useEsClasses = true
}