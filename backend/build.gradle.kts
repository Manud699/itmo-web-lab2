plugins {
    id("java")
}

group = "ru.itmo.web"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(files("lib/fastcgi-lib.jar"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> { options.release = 17 }
// Runnable "fat" jar: our classes + the FastCGI library, started with `java -jar`
tasks.jar {
    archiveFileName.set("lab2-server.jar")
    manifest { attributes["Main-Class"] = "ru.itmo.web.lab2.AppMain" }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
