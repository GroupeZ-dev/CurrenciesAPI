plugins {
    java
    `maven-publish`
}

group = "fr.traqueur"
version = property("version") as String

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
        name = "jitpack"
    }
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        name = "spigot-repo"
    }
    maven {
        url = uri("https://nexus.bencodez.com/repository/maven-public/")
        name = "BenCodez Repo"
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("com.github.PlayerNguyen:OptEco:2.1.4b")
    compileOnly("com.willfp:EcoBits:1.8.4")
    compileOnly("com.bencodez:votingplugin:6.17.2")
    compileOnly("com.github.Emibergo02:RedisEconomy:4.3.19")

    compileOnly(files("libs/bt-api-3.14.6.jar"))
    compileOnly(files("libs/MySQLTokens.jar"))
    compileOnly(files("libs/PlayerPoints-3.2.7.jar"))
    compileOnly(files("libs/TokenAPI.jar"))
    compileOnly(files("libs/GemAPI.jar"))
    compileOnly(files("libs/zEssentialsAPI-1.0.1.4.jar"))
    compileOnly(files("libs/CoinsEngine-2.4.2.jar"))
    compileOnly(files("libs/nightcore-2.7.1.jar"))
    compileOnly(files("libs/RoyaleEconomyAPI.jar"))
    compileOnly(files("libs/zMenu-API-1.1.0.0.jar"))
}

val targetJavaVersion = 8

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }

    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

val generateVersionProperties by tasks.registering {
    doLast {
        val file = file("src/main/resources/${project.name.lowercase()}.properties")
        file.parentFile.mkdirs()
        file.writeText("version=${project.version}")
    }
}

tasks.named("build") {
    dependsOn(generateVersionProperties)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
