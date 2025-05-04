plugins {
    `java-library`
    signing

}

repositories {
    mavenCentral()
}

signing {
    useGpgCmd()
    sign( publishing.publications)
}

dependencies {
    annotationProcessor(libs.auto.service)
    implementation(libs.auto.service.annotations)
    implementation(libs.javapoet)
    implementation(project(":auto-codec-annotation"))
}

