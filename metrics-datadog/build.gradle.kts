plugins {
    id("pro.streem.metrics-datadog.java-conventions")
    id("pro.streem.metrics-datadog.publish-conventions")
}

description = "A Datadog reporter backend for Coda Hale's Metrics"

dependencies {
    api(libs.dropwizard.metrics.core)
    api(libs.httpcomponents.fluentHc)
    api(libs.jackson.databind)
    api(libs.dogstatsd.client)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.all)
    testImplementation(libs.slf4j.simple)
    testImplementation(libs.dnsCacheManipulator)
}
