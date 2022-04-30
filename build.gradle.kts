buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.6.21")
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.1")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
    group = "com.github.sieunju"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}