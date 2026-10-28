
plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

tasks.withType<Checkstyle>().configureEach {
    exclude("ca/teamdman/langs/**")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    systemProperty("sfm.phaseProfile", providers.gradleProperty("phaseProfile").getOrElse("false"))
    systemProperty("sfm.auditBenchmark", providers.gradleProperty("auditBenchmark").getOrElse("false"))
    systemProperty("sfm.characterizationSeed", providers.gradleProperty("characterizationSeed").getOrElse("1037"))
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
