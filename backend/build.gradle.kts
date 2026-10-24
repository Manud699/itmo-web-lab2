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