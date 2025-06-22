import de.undercouch.gradle.tasks.download.Download

plugins {
    id("java")
    id("de.undercouch.download").version("5.6.0")
}

group = "dk.hydrozoa"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.eclipse.jetty:jetty-server:12.0.22")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    implementation("org.freemarker:freemarker:2.3.34")
    implementation("com.google.guava:guava:33.4.8-jre")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Download>("downloadBootstrap") {
    description = "Downloads bootstrap"

    val cdnUrl = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.7/dist/"

    val cssUrl = cdnUrl + "css/bootstrap.min.css"
    val jsUrl = cdnUrl + "js/bootstrap.bundle.min.js"

    src(
        arrayOf(
            cssUrl,
            jsUrl
        )
    )

    val destinationDirectory = projectDir.resolve("data/public/lib/bootstrap").canonicalFile
    dest(destinationDirectory)
    overwrite(false)
    onlyIfModified(true)

    doFirst {
        destinationDirectory.mkdirs()
    }
}