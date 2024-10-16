
plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("maven-publish")
}

group = "com.github.EngMotion"
version = "1.3.6"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")

}

dependencies {
    implementation("io.github.java-native:jssc:2.9.6")
    implementation("com.fazecast:jSerialComm:2.11.0")
    implementation("com.ghgande:j2mod:3.2.1")
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

}

tasks.test {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

publishing() {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

tasks {
    javadoc {
        destinationDir = file("build/docs/javadoc")
    }

}