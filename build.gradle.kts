plugins {
    alias(libs.plugins.maven.publish)
}

allprojects {
    group = "io.github.iakariak"
    version = "0.0.1"

    repositories {
        mavenCentral()
    }


}

subprojects {
    apply(plugin = "java-library")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
}