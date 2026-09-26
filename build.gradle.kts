
plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

tasks.withType<Checkstyle>().configureEach {
    exclude("ca/teamdman/langs/**")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
