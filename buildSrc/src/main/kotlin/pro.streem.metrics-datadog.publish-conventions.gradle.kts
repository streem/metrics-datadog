import com.google.common.base.CaseFormat

plugins {
    `maven-publish`
    signing
}

val sonatypeApiUser = providers.gradlePropertyOrEnvironmentVariable("sonatypeApiUser")
val sonatypeApiKey = providers.gradlePropertyOrEnvironmentVariable("sonatypeApiKey")
val sonatypeRepositoryId = providers.gradlePropertyOrEnvironmentVariable("sonatypeRepositoryId")
val publishToSonatype = sonatypeApiUser.isPresent && sonatypeApiKey.isPresent
if (!publishToSonatype) {
    logger.info("Sonatype API key not defined, skipping configuration of Maven Central publishing repository")
}

val signingKeyAsciiArmored = providers.gradlePropertyOrEnvironmentVariable("signingKeyAsciiArmored")
if (signingKeyAsciiArmored.isPresent) {
    signing {
        useInMemoryPgpKeys(signingKeyAsciiArmored.get(), "")
        sign(extensions.getByType<PublishingExtension>().publications)
    }
} else {
    logger.info("PGP signing key not defined, skipping signing configuration")
}


publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
        pom {
            name.set(artifactId)
            url.set("https://github.com/streem/metrics-datadog")

            licenses {
                license {
                    name.set("The BSD 3-Clause License")
                    url.set("http://opensource.org/licenses/BSD-3-Clause")
                }
            }

            organization {
                name.set("Streem")
                url.set("https://github.com/streem")
            }

            developers {
                developer {
                    id.set("streem")
                    name.set("Streem")
                    url.set("https://github.com/streem")
                }
            }

            contributors {
                contributor {
                    name.set("David Guo")
                    email.set("dguo (at) coursera.org")
                }
                contributor {
                    name.set("Ankur Chauhan")
                    email.set("ankur (at) malloc64.com")
                }

                contributor {
                    name.set("Arup Malakar")
                    email.set("amalakar (at) gmail.com")
                }

                contributor {
                    name.set("Eduardo Narros")
                }

                contributor {
                    name.set("Joe Hohertz")
                }
            }

            scm {
                connection.set("scm:git:git@github.com:streem/metrics-datadog.git")
                developerConnection.set("scm:git:git@github.com:streem/metrics-datadog.git")
                url.set("scm:git@github.com:streem/metrics-datadog.git")
            }
        }
    }

    if (publishToSonatype) {
        repositories {
            maven {
                name = "sonatype"
                url = when {
                    project.version.toString().endsWith("-SNAPSHOT") ->
                        uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")

                    !sonatypeRepositoryId.isPresent ->
                        throw IllegalStateException("Sonatype Repository ID must be provided for non-SNAPSHOT builds")

                    else ->
                        uri("https://s01.oss.sonatype.org/service/local/staging/deployByRepositoryId/${sonatypeRepositoryId.get()}")
                }

                credentials {
                    username = sonatypeApiUser.get()
                    password = sonatypeApiKey.get()
                }
            }
        }
    }
}

/**
 * Creates a [Provider] whose value is fetched from the Gradle property named [propertyName], or if there is no such
 * Gradle property, then from the environment variable whose name is the ALL_CAPS version of [propertyName]. For
 * example, given a [propertyName] of "fooBar", this function will look for an environment variable named "FOO_BAR".
 */
fun ProviderFactory.gradlePropertyOrEnvironmentVariable(propertyName: String): Provider<String> {
    val envVariableName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, propertyName)
    return gradleProperty(propertyName).orElse(environmentVariable(envVariableName))
}