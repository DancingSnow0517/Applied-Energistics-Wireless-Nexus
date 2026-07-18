
plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.named<Jar>("jar") {
    from("guidenh") {
        into("assets/${providers.gradleProperty("modId").get()}/guidenh")
    }
}

tasks.configureEach {
    if (name.startsWith("runClient") && this is JavaExec) {
        systemProperty("guideme.appliedenergistics2.guidenh.sources", file("guidenh").absolutePath)
        systemProperty("guideme.appliedenergistics2.guidenh.sourcesNamespace", "ae_wireless_nexus")
    }
}
