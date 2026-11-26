import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.4.21"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.firebase:firebase-admin:9.12.0")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }

    sourceSets {
        main {
            // shares the selection logic and model with the android app
            kotlin.srcDir("../code/app/src/main/java")

            kotlin.include(
                "worker/**",
                "com/example/staaks_campsite_app/data/LotteryDraw.kt",
                "com/example/staaks_campsite_app/data/model/WaitlistEntry.kt"
            )
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("worker.MainKt")
}