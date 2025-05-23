plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("maven-publish")
}

group = "com.github.EngMotion"
version = "1.4.7"

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
    implementation("com.intellij:forms_rt:7.0.3")
    implementation("com.jgoodies:forms:1.1-preview")
    implementation("de.exlll:configlib-yaml:4.5.0")
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
        options {
            (this as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
            (this as CoreJavadocOptions).addStringOption("tag", "noinspection:a:\"\"")
        }
    }
}