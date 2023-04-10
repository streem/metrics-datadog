plugins {
    id("pro.streem.metrics-datadog.java-conventions")
    id("pro.streem.metrics-datadog.publish-conventions")
}

description = "Dropwizard Datadog Reporter"

dependencies {
    api(project(":metrics-datadog"))
    api(libs.dropwizard.metrics.dropwizard)
    api(libs.jakarta.validation.api)
    testImplementation(libs.junit)
    testImplementation(libs.fest.assert.core)
}