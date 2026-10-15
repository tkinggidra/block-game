
plugins {
    application
}

// Project metadata
group = "io.github.tkinggidra"
version = providers.gradleProperty("projectVersion").get()

// Repositories
repositories {
    mavenCentral()
}

// Java
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

// Dependency versions
val lwjglVersion = "3.4.1"
val imguiVersion = "1.92.7.1"
val jomlVersion = "1.10.8"
val junitVersion = "6.0.0"

// Dependencies
dependencies {
    // Engine
    implementation(project(":engine"))

    // LWJGL
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-glfw")
    implementation("org.lwjgl:lwjgl-opengl")

    // LWJGL natives (Windows)
    runtimeOnly("org.lwjgl:lwjgl::natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-glfw::natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-opengl::natives-windows")

    // Dear ImGui
    implementation("io.github.spair:imgui-java-binding:$imguiVersion")
    implementation("io.github.spair:imgui-java-lwjgl3:$imguiVersion")
    runtimeOnly("io.github.spair:imgui-java-natives-windows:$imguiVersion")

    // Mathematics
    implementation("org.joml:joml:$jomlVersion")

    // Testing
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Application
application {
    mainClass = "io.github.tkinggidra.blockgame.client.Main"

    applicationDefaultJvmArgs = listOf(
        "--enable-native-access=ALL-UNNAMED",
        "-Djoml.nounsafe=true"
    )
}

// Generated resources
val generateVersionResource by tasks.registering {
    val outputDirectory = layout.buildDirectory.dir("generated/version")
    val currentVersion = providers.gradleProperty("projectVersion")

    inputs.property("version", currentVersion)
    outputs.dir(outputDirectory)

    doLast {
        val file = outputDirectory.get()
            .file("blockgame.properties")
            .asFile

        file.parentFile.mkdirs()
        file.writeText("version=${currentVersion.get()}\n")
    }
}

sourceSets.main {
    resources.srcDir(generateVersionResource)
}

// Testing
tasks.test {
    useJUnitPlatform()
}
