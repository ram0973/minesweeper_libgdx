import org.gradle.jvm.tasks.Jar

plugins {
    java
}

group = "org.ram0973"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val gdxVersion = "1.9.14"
    testImplementation("junit", "junit", "4.12")
    implementation("com.badlogicgames.gdx:gdx:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
}

val fatJar = task("fatJar", type = Jar::class) {
    manifest {
        attributes["Implementation-Title"] = "Minesweeper with LibGDX"
        attributes["Main-Class"] = "minesweeper.MineSweeper"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}