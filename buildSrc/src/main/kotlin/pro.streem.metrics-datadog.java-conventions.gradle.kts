plugins {
    `java-library`
}

group = "pro.streem.metrics-datadog"
version = "2.0.1"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    toolchain.languageVersion.set(JavaLanguageVersion.of(11))

    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
    (options as? StandardJavadocDocletOptions)?.addBooleanOption("Xdoclint:none", true)
}
