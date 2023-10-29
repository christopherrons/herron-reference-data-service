dependencies {
    // External Libs
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.kafka)
    implementation(libs.commons.math)

    // Internal Libs
    implementation(libs.common.api)
    implementation(libs.common)
    implementation(libs.integrations)
    implementation(libs.integration)

    // External Test Libs
    testImplementation(testlibs.junit.jupiter.api)
    testImplementation(testlibs.junit.jupiter.engine)
    testImplementation(testlibs.spring.boot.starter.test)
}

tasks.test {
    useJUnitPlatform()
}