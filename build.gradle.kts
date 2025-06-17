plugins {
    java
}

group = "com.gplugins"
version = "1.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    // Paper API pour 1.21.5
    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
    
    // Annotations pour éviter les warnings
    compileOnly("org.jetbrains:annotations:24.0.1")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<Jar> {
    archiveFileName.set("GPlugins.jar")
    
    manifest {
        attributes(
            "Main-Class" to "com.gplugins.Main"
        )
    }
}

// Tâche pour copier le plugin dans le dossier plugins local (optionnel)
tasks.register<Copy>("deployPlugin") {
    dependsOn("jar")
    from(layout.buildDirectory.file("libs/GPlugins.jar"))
    into("run/plugins/")
}