plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.0.13"
}

group = "com.anadumitrache"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.xerial:sqlite-jdbc:3.45.3.0")
    implementation("org.slf4j:slf4j-simple:2.0.13")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

javafx {
    version = "21"
    modules = listOf("javafx.controls")
}

application {
    mainClass.set("searchengine.ui.SearchFxApp")
}

tasks.test {
    useJUnitPlatform()
}