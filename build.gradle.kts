plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.lucaf"
version = "1.0"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")

}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("io.github.java-native:jssc:2.9.6")
    implementation("com.fazecast:jSerialComm:2.11.0")
    implementation("com.ghgande:j2mod:3.2.1")
    compileOnly("org.projectlombok:lombok:1.18.34")
}

tasks.test {
    useJUnitPlatform()
}

