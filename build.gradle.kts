plugins {
    java
}

group = "com.aikero.otel"
version = libs.versions.otelAgent.get()

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

dependencies {
    // OTel Agent Extension API (provided by agent at runtime)
    compileOnly(platform(libs.opentelemetry.bom))
    compileOnly(libs.opentelemetry.javaagent.extension.api)
    compileOnly(libs.opentelemetry.api)

    // AutoService annotation processor (generates META-INF/services at compile time)
    annotationProcessor(libs.auto.service)
    compileOnly(libs.auto.service.annotations)

    // Test
    testImplementation(platform(libs.opentelemetry.bom))
    testImplementation(libs.opentelemetry.javaagent.extension.api)
    testImplementation(libs.opentelemetry.api)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
