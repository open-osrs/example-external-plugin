import ProjectVersions.openosrsVersion

version = "0.0.1"

project.extra["PluginName"] = "Custom One Click"
project.extra["PluginDescription"] = "One clicks just for us."
project.extra["PluginProvider"] = "tha23rd"

dependencies {
    annotationProcessor(Libraries.lombok)
    annotationProcessor(Libraries.slf4j)
    annotationProcessor(Libraries.pf4j)

    compileOnly(Libraries.lombok)
    compileOnly(Libraries.slf4j)
    compileOnly(Libraries.pf4j)
    compileOnly(Libraries.guice)
    compileOnly(Libraries.javax)

    compileOnly("com.openosrs:runelite-api:$openosrsVersion+")
    compileOnly("com.openosrs:runelite-client:$openosrsVersion+")

    implementation(Libraries.json)
    implementation(Libraries.jsoup)
    implementation(Libraries.gson)
}

tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) })
        val sourcesMain = sourceSets.main.get()
        from(sourcesMain.output)

        manifest {
            attributes(mapOf(
                "Plugin-Version" to project.version,
                "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                "Plugin-Provider" to project.extra["PluginProvider"],
                "Plugin-Description" to project.extra["PluginDescription"],
                "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}