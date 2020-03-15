buildscript {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    checkstyle
}

project.extra["GithubUrl"] = "http://github.com/<INSERT NAME>/<INSERT REPOSITORY>"

apply<BootstrapPlugin>()

allprojects {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}

subprojects {
    group = "com.example"

    project.extra["PluginProvider"] = ""
    project.extra["ProjectSupportUrl"] = ""
    project.extra["PluginLicense"] = "3-Clause BSD License"

    repositories {
        jcenter {
            content {
                excludeGroupByRegex("com\\.openosrs.*")
            }
        }

        exclusiveContent {
            forRepository {
                maven {
                    url = uri("https://jitpack.io")
                }
            }
            filter {
                includeGroupByRegex("com\\.openosrs.*")
            }
        }
    }

    apply<JavaPlugin>()

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }

        withType<AbstractArchiveTask> {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
            dirMode = 493
            fileMode = 420
        }
    }
}