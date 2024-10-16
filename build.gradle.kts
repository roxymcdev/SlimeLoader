plugins {
    id("java")
    id("java-library")
    id("maven-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    api("com.google.guava:guava:33.3.1-jre")
    api("com.google.code.gson:gson:2.11.0")
    api("org.jspecify:jspecify:1.0.0")
    api("net.kyori:adventure-nbt:4.17.0")
    api("com.github.luben:zstd-jni:1.5.6-6")

    testCompileOnly("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)

    withSourcesJar()
}

tasks {
    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release = 17
        dependsOn(clean)
    }

    test {
        useJUnitPlatform()
    }
}


publishing {
    repositories {
        val repoType = if (version.toString().endsWith("-SNAPSHOT")) "snapshots" else "releases"
        maven("https://repo.roxymc.net/${repoType}") {
            name = "roxymc"
            credentials(PasswordCredentials::class)
        }
    }

    publications {
        create<MavenPublication>("maven") {
            artifactId = rootProject.name.lowercase()

            from(components["java"])
        }
    }
}
