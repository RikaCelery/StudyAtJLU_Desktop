import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "rikacelery.github.io"
//MAJOR.MINOR.BUILD
//255.255.65535
version = "1.0.21"

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
                implementation("org.jsoup:jsoup:1.16.1")
                val ktorVersion = "2.3.0"
                implementation("io.ktor:ktor-client-okhttp-jvm:$ktorVersion")
                implementation("io.ktor:ktor-client-logging-jvm:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-xml:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-logging:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-client-encoding:$ktorVersion")
                implementation("io.ktor:ktor-client-websockets:$ktorVersion")
                implementation(compose.desktop.currentOs)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines:0.19.2")
                implementation("com.typesafe:config:1.4.1")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            windows {
                perUserInstall = true
                shortcut = true
            }
            macOS {
                dockName = "StudyAtJLU_Desktop" + version.toString().substringAfter('-', "").let {
                    if (it.isNotEmpty()) "_$it" else it
                }
                pkgPackageVersion = version.toString().substringBeforeLast('.')
                pkgPackageBuildVersion = version.toString().substringAfterLast('.').substringBefore('-')
            }
            targetFormats(TargetFormat.Msi,TargetFormat.Dmg)
            packageName = "StudyAtJLU_Desktop" + version.toString().substringAfter('-', "").let {
                if (it.isNotEmpty()) "_$it" else it
            }
            packageVersion = version.toString().substringBefore('-')
        }
    }
}
