import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "rikacelery.github.io"
version = "1.0-SNAPSHOT"

repositories {
            google()
            mavenCentral()
            maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        }

kotlin {
    jvm {
        jvmToolchain(11)
        withJava()
    }
    sourceSets {

        val jvmMain by getting {

            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines:0.19.2")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "StudyAtJLU_Desktop"
            packageVersion = "1.0.0"
        }
    }
}
