
plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

// Release assets keep the target visible in their filenames and in Forge's mod version.
providers.gradleProperty("sfmVersion").orNull?.let { release ->
    require(release.matches(Regex("[0-9][A-Za-z0-9.+-]*"))) { "Invalid sfmVersion" }
    val targetVersion = "$release-${extra["sfmTargetSuffix"]}"
    version = targetVersion
    tasks.named<com.gtnewhorizons.retrofuturagradle.mcp.InjectTagsTask>("injectTags") {
        tags.put("VERSION", targetVersion)
    }
}

tasks.withType<Checkstyle>().configureEach {
    exclude("ca/teamdman/langs/**")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    // The guide corpus is read by SfmlGuideTest and must invalidate cached test results after edits.
    inputs.file("docs/SFML_GUIDE.md").withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.dir("src/main/resources/assets/superfactorymanager/guidenh").withPathSensitivity(PathSensitivity.RELATIVE)
    val guideNhJar = providers.gradleProperty("guideNhJar").getOrElse("")
    systemProperty("sfm.guideNhJar", guideNhJar)
    if (guideNhJar.isNotEmpty()) inputs.file(guideNhJar).withPathSensitivity(PathSensitivity.NONE)
    val guideNhRuntimeJar = providers.gradleProperty("guideNhRuntimeJar").getOrElse("")
    systemProperty("sfm.guideNhRuntimeJar", guideNhRuntimeJar)
    if (guideNhRuntimeJar.isNotEmpty()) inputs.file(guideNhRuntimeJar).withPathSensitivity(PathSensitivity.NONE)
    systemProperty("sfm.phaseProfile", providers.gradleProperty("phaseProfile").getOrElse("false"))
    systemProperty("sfm.auditBenchmark", providers.gradleProperty("auditBenchmark").getOrElse("false"))
    systemProperty("sfm.characterizationSeed", providers.gradleProperty("characterizationSeed").getOrElse("1037"))
}

// The same pages ship in the mod JAR and in an optional resource pack for content-only updates.
tasks.register<Zip>("guideNhPack") {
    group = "documentation"
    description = "Package the SFM GuideNH book without game code or a GuideNH runtime dependency."
    archiveFileName.set("SFM-GuideNH-en_us.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    from("src/main/resources") { include("assets/superfactorymanager/guidenh/**") }
    from("guidepack") { include("pack.mcmeta") }
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

// Fixture commands and their geometry tests never enter ordinary release artifacts.
if (providers.gradleProperty("packBenchmark").getOrElse("false").toBoolean()) {
    sourceSets.named("main") { java.srcDir("src/perf/java") }
    sourceSets.named("test") { java.srcDir("src/perfTest/java") }
    tasks.withType<AbstractArchiveTask>().configureEach {
        archiveBaseName.set("superfactorymanager-benchmark")
    }
}

// Development launches use the same user-selected heap ceiling as our pack fixture.
val clientTestHarness = providers.gradleProperty("clientTestHarness").getOrElse("false").toBoolean()
tasks.withType<JavaExec>().configureEach {
    if (name == "runClient" || name == "runServer") {
        minHeapSize = "512M"
        maxHeapSize = "4G"
        if (name == "runClient" && clientTestHarness) systemProperty("sfm.testHarness", "true")
    }
}

// Regenerate the checked-in parser when extending the language; never run ANTLR on a server tick.
val sfmlParserGenerator by configurations.creating
 dependencies.add(sfmlParserGenerator.name, "org.antlr:antlr4:4.13.2")
tasks.register<JavaExec>("generateSfmlParser") {
    group = "development"
    description = "Regenerate the checked-in SFML Java lexer, parser and visitors."
    classpath = sfmlParserGenerator
    mainClass.set("org.antlr.v4.Tool")
    workingDir = projectDir
    val grammar = "src/main/resources/assets/superfactorymanager/grammar/SFML.g4"
    val output = "src/main/java/ca/teamdman/langs"
    inputs.file(grammar)
    outputs.dir(output)
    args("-visitor", "-Xexact-output-dir", "-o", output, grammar)
}

// Checked-in generated sources also belong to normal source/format tasks. Order explicit regeneration first.
tasks.matching { it.name in setOf("compileJava", "sourcesJar", "spotlessJava", "checkstyleMain") }.configureEach {
    mustRunAfter(tasks.named("generateSfmlParser"))
}

// Keep launch metadata in task properties so configuration-cache reuse is supported.
abstract class WriteManualClientArtifactTask : DefaultTask() {
    @get:InputFiles
    abstract val artifacts: ConfigurableFileCollection

    @get:OutputFile
    abstract val manifest: RegularFileProperty

    @TaskAction
    fun recordArtifact() {
        val jars = artifacts.files.filter { it.isFile && it.extension == "jar" }
        check(jars.size == 1) { "Expected one production JAR from reobfJar, got $jars" }
        manifest.get().asFile.writeText(jars.single().absolutePath + "\n")
    }
}

// Use this build's artifact even when other branches left newer JARs behind.
tasks.register<WriteManualClientArtifactTask>("writeManualClientArtifact") {
    group = "development"
    description = "Record the exact production JAR for the isolated Prism development instance."
    artifacts.from(tasks.named("reobfJar"))
    manifest.set(layout.buildDirectory.file("manual-client-artifact.txt"))
}

// The editor compiler bridge is a development tool, never part of main/reobfJar or a VSIX.
val tooling by sourceSets.creating {
    java.srcDir("src/tooling/java")
    // The bridge is ordinary Java 8. Do not expose javac to main's automatically discovered Jabel plugin.
    compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath.filter {
        !it.name.startsWith("jabel-javac-plugin-")
    }
}
tasks.named<JavaCompile>(tooling.compileJavaTaskName) {
    options.annotationProcessorPath = files()
}
// Plain headless tests already resolve Minecraft/Forge and optional signature dependencies correctly.
val sfmlCompilerClasspath = files(
    tooling.output,
    tasks.named<Test>("test").get().classpath.minus(sourceSets.test.get().output)
)
val sfmlCompilerJava = javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(17))
}

val checkSfmlCompiler by tasks.registering(JavaExec::class) {
    group = "development"
    description = "Check the actual SFML compiler on the offline editor-tool classpath without running a program."
    dependsOn(tasks.named(tooling.classesTaskName), tasks.named("classes"))
    javaLauncher.set(sfmlCompilerJava)
    classpath = sfmlCompilerClasspath
    mainClass.set("ca.teamdman.sfm.tooling.SfmlCompilerServer")
    args("--check")
}

abstract class WriteSfmlCompilerManifestTask : DefaultTask() {
    @get:Classpath
    abstract val compilerClasspath: ConfigurableFileCollection

    @get:Input
    abstract val javaExecutable: Property<String>

    @get:OutputFile
    abstract val manifest: RegularFileProperty

    @TaskAction
    fun writeManifest() {
        val destination = manifest.get().asFile
        destination.parentFile.mkdirs()
        destination.writeText(groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(mapOf(
            "javaExecutable" to javaExecutable.get(),
            "classpath" to compilerClasspath.files.map { it.absolutePath },
            "mainClass" to "ca.teamdman.sfm.tooling.SfmlCompilerServer"
        ))) + "\n")
    }
}

val prepareSfmlCompiler by tasks.registering(WriteSfmlCompilerManifestTask::class) {
    group = "development"
    description = "Compile/check the development SFML compiler bridge and export its local launch manifest."
    dependsOn(checkSfmlCompiler)
    compilerClasspath.from(sfmlCompilerClasspath)
    javaExecutable.set(sfmlCompilerJava.map { it.executablePath.asFile.absolutePath })
    manifest.set(layout.buildDirectory.file("sfml-compiler.json"))
}

tasks.named<Test>("test") {
    dependsOn(prepareSfmlCompiler)
    systemProperty("sfm.compilerManifest", layout.buildDirectory.file("sfml-compiler.json").get().asFile.absolutePath)
}
