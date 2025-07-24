import de.undercouch.gradle.tasks.download.Download

plugins {
    id("java")
    id("application")
    id("de.undercouch.download").version("5.6.0")
    id("com.github.johnrengelman.shadow").version("8.1.1")
}

application {
    mainClass.set("dk.hydrozoa.hydrowiki.Main")
}

group = "dk.hydrozoa"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.eclipse.jetty:jetty-server:12.0.23")
    implementation("org.eclipse.jetty:jetty-session:12.0.23")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    implementation("org.freemarker:freemarker:2.3.34")
    implementation("com.google.guava:guava:33.4.8-jre")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.3")
    implementation("io.github.java-diff-utils:java-diff-utils:4.15")

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

// New task to compile disstribution of the application
tasks.register("distributeApp") {
    description = "Prepares the application for distribution by copying necessary files."

    // This task depends on the 'shadowJar' task to ensure the fat JAR is built first.
    dependsOn(tasks.shadowJar)

    doLast {
        // Define the distribution directory
        val distDir = projectDir.resolve("dis")
        distDir.mkdirs() // Ensure the 'dis' directory exists

        // Define source and destination directories for data and templates
        val publicDir = projectDir.resolve("data/public")
        val publicDestDir = distDir.resolve("data/public")
        val templateSourceDir = projectDir.resolve("data/template")
        val templateDestDir = distDir.resolve("data/template")

        // Delete existing public and template directories in 'dis' if they exist
        if (publicDestDir.exists()) {
            println("Deleting existing directory: ${publicDestDir.absolutePath}")
            publicDestDir.deleteRecursively()
        }
        if (templateDestDir.exists()) {
            println("Deleting existing directory: ${templateDestDir.absolutePath}")
            templateDestDir.deleteRecursively()
        }

        // Copy 'data/public' to 'dis/data/public'
        if (publicDir.exists()) {
            println("Copying ${publicDir.absolutePath} to ${publicDestDir.absolutePath}")
            publicDir.copyRecursively(publicDestDir, overwrite = true)
        } else {
            println("Warning: Source directory not found: ${publicDir.absolutePath}")
        }

        // Copy 'data/template' to 'dis/data/template'
        if (templateSourceDir.exists()) {
            println("Copying ${templateSourceDir.absolutePath} to ${templateDestDir.absolutePath}")
            templateSourceDir.copyRecursively(templateDestDir, overwrite = true)
        } else {
            println("Warning: Source directory not found: ${templateSourceDir.absolutePath}")
        }

        // Copy the generated JAR to 'dis'
        val jarFile = tasks.shadowJar.get().archiveFile.get().asFile
        val destJarFile = distDir.resolve("hydrowiki.jar")
        if (jarFile.exists()) {
            println("Copying ${jarFile.absolutePath} to ${destJarFile.absolutePath}")
            jarFile.copyTo(destJarFile, overwrite = true)
        } else {
            println("Error: JAR file not found at ${jarFile.absolutePath}. Please run 'gradle shadowJar' first.")
        }

        val versionFile = distDir.resolve("version.txt")
        versionFile.writeText(project.version.toString())
        println("Created version file: ${versionFile.absolutePath} with content: ${project.version}")
    }
}