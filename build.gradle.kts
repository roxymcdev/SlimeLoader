subprojects {
    plugins.apply("java")
    plugins.apply("java-library")
    plugins.apply("maven-publish")

    repositories {
        mavenCentral()
    }

    afterEvaluate {
        dependencies {
            "testCompileOnly"(libs.lombok)
            "testAnnotationProcessor"(libs.lombok)
            "testImplementation"(platform(libs.junit5.bom))
            "testImplementation"(libs.junit5.jupiter)
        }
    }

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion = JavaLanguageVersion.of(17)

        withSourcesJar()
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = Charsets.UTF_8.name()
            options.release = 17
            dependsOn("clean")
        }

        withType<Test> {
            useJUnitPlatform()
        }
    }

    extensions.configure<PublishingExtension> {
        repositories {
            val repoType = if (version.toString().endsWith("-SNAPSHOT")) "snapshots" else "releases"
            maven("https://repo.roxymc.net/${repoType}") {
                name = "roxymc"
                credentials(PasswordCredentials::class)
            }
        }

        publications {
            create<MavenPublication>("maven") {
                artifactId = project.name

                from(components["java"])
            }
        }
    }
}
