plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // mongo db drivers
    implementation("org.mongodb:mongodb-driver-sync:4.10.2")

    // lucene package
    implementation ("org.apache.lucene:lucene-core:8.11.1")
    implementation ("org.apache.lucene:lucene-analyzers-common:8.11.1")
    implementation ("org.apache.lucene:lucene-queryparser:8.11.1")
}

tasks.test {
    useJUnitPlatform()
}