import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.compose") version "1.1.0"
}

group = "me.zhangxinkun"
version = "1.0"

repositories {
    mavenLocal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("com.xinky:jcef-demo:1.0-SNAPSHOT")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

val theMainJar = File(project.buildDir, "main.jar")


tasks.register("checkJBR"){
    val javaHome = File(System.getProperty("java.home"))
    val cef = File(
        javaHome.parent,
        "Frameworks/Chromium Embedded Framework.framework/Chromium Embedded Framework"
    )
    if (cef.exists() && cef.isFile) {
        println("Found Chromium Embedded Framework at ${cef.absolutePath}")
    } else {
        throw IllegalStateException("jdk无法正确发布此工程，请使用Jetbrains Runtime！")
    }
}
tasks.register("copyBootJar") {
    dependsOn("checkJBR")
    doLast {
        val runtime = configurations.runtimeClasspath.get()
        runtime.files.forEach {
            if (it.name.contains("jcef-demo")) {
                println("use boot jar: $theMainJar")
                it.copyTo(theMainJar, overwrite = true)
            }
        }
    }
}

tasks.getByPath("jar").dependsOn("copyBootJar")

compose.desktop {
    application {
        disableDefaultConfiguration()
        dependsOn("copyBootJar")
        mainJar.set(theMainJar)
        mainClass = "org.springframework.boot.loader.JarLauncher"
        nativeDistributions {
            appResourcesRootDir.set(project.layout.projectDirectory.dir("package/resources"))
            modules("jcef","java.management","java.naming","java.desktop","java.security.jgss","java.instrument")
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "untitled"
            packageVersion = "1.0.0"
        }
    }

}


