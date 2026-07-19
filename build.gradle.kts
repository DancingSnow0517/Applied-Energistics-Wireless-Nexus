plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

if (file("addon.gradle").exists()) {
    apply(from = "addon.gradle")
}

val modId: String by project

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.named<Jar>("jar") {
    from("guidenh") {
        into("assets/${modId}/guidenh")
    }
}

tasks.configureEach {
    if (name.startsWith("runClient") && this is JavaExec) {
        systemProperty("guideme.appliedenergistics2.guidenh.sources", file("guidenh").absolutePath)
        systemProperty("guideme.appliedenergistics2.guidenh.sourcesNamespace", modId)
    }
}
