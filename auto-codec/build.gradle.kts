plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor(libs.auto.service)
    implementation(libs.auto.service.annotations)
    implementation(libs.javapoet)
    implementation(project(":auto-codec-annotation"))
}

