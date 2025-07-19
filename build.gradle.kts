import com.modrinth.minotaur.dependencies.ModDependency
import dev.lambdaurora.mcdev.api.McVersionLookup
import dev.lambdaurora.mcdev.api.ModUtils
import net.darkhax.curseforgegradle.TaskPublishCurseForge

plugins {
	id("fabric-loom").version("1.10.+")
	id("dev.lambdaurora.mcdev").version("1.0.+")
	id("dev.yumi.gradle.licenser").version("2.+")
	id("com.modrinth.minotaur").version("2.+")
	id("net.darkhax.curseforgegradle").version("1.1.+")
}

group = project.property("maven_group") as String
base.archivesName.set(project.property("archives_base_name") as String)

val mcVersion = libs.versions.minecraft.get()
val VERSION = project.property("mod_version") as String
version = "$VERSION+$mcVersion"

// This field defines the Java version your mod target.
val targetJavaVersion = 21

val compatibleMinecraftVersions = listOf("1.21.7", "1.21.6")

repositories {
	maven {
		name = "Gegy"
		url = uri("https://maven.gegy.dev/releases/")
	}
}

dependencies {
	//to change the versions see the gradle.properties file
	minecraft(libs.minecraft)
	@Suppress("UnstableApiUsage")
	mappings(lambdamcdev.layered {
		officialMojangMappings()
		mappings("dev.lambdaurora:yalmm:${mcVersion}+build.${libs.versions.mappings.yalmm.get()}")
	})
	modImplementation(libs.fabric.loader)

	modImplementation(libs.fabric.api)
}

java {
	sourceCompatibility = JavaVersion.toVersion(targetJavaVersion)
	targetCompatibility = JavaVersion.toVersion(targetJavaVersion)

	withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.isDeprecation = true
	options.isIncremental = true
	options.release.set(targetJavaVersion)
}

tasks.processResources {
	inputs.property("version", project.version)

	filesMatching("fabric.mod.json") {
		expand("version" to inputs.properties["version"])
	}
}

tasks.jar {
	from("LICENSE") {
		rename { "${it}_${base.archivesName.get()}" }
	}
}

license {
	rule(rootProject.file("codeformat/HEADER"))
}

modrinth {
	projectId = project.property("modrinth_id") as String
	versionName = "Lovely Snails $VERSION (${McVersionLookup.getVersionTag(mcVersion)})"
	versionType.set(ModUtils.fetchVersionType(VERSION, mcVersion))
	uploadFile.set(tasks.remapJar.get())
	loaders.set(listOf("fabric", "quilt"))
	gameVersions.set(listOf(mcVersion) + compatibleMinecraftVersions)
	dependencies.set(
		listOf(
			ModDependency("P7dR8mSH", "required") // Fabric API
		)
	)
	syncBodyFrom.set(
		ModUtils.parseReadme(
			project, "https://raw.githubusercontent.com/LambdAurora/lovely_snails/1.21/\$2"
		)
	)

	// Changelog fetching
	val changelogContent = ModUtils.fetchChangelog(project, VERSION)

	if (changelogContent != null) {
		changelog = changelogContent
	} else {
		afterEvaluate {
			tasks.modrinth.get().isEnabled = false
		}
	}
}
tasks.modrinth {
	dependsOn(tasks.modrinthSyncBody)
}

tasks.register<TaskPublishCurseForge>("curseforge") {
	this.group = "publishing"

	val token = System.getenv("CURSEFORGE_TOKEN")
	if (token != null) {
		this.apiToken = token
	} else {
		this.isEnabled = false
		return@register
	}

	// Changelog fetching
	var changelogContent = ModUtils.fetchChangelog(project, VERSION)

	if (changelogContent != null) {
		changelogContent = "Changelog:\n\n${changelogContent}"
	} else {
		this.isEnabled = false
		return@register
	}

	val mainFile = upload(project.property("curseforge_id"), tasks.remapJar.get())
	mainFile.releaseType = ModUtils.fetchVersionType(VERSION, mcVersion)
	mainFile.addGameVersion(McVersionLookup.getCurseForgeEquivalent(mcVersion))
	compatibleMinecraftVersions.stream()
		.map { McVersionLookup.getCurseForgeEquivalent(it) }
		.forEach { mainFile.addGameVersion(it) }
	mainFile.addModLoader("Fabric", "Quilt")
	mainFile.addJavaVersion("Java 21", "Java 22")

	mainFile.displayName = "Lovely Snails $VERSION (${McVersionLookup.getVersionTag(mcVersion)})"
	mainFile.addRequirement("fabric-api")

	mainFile.changelogType = "markdown"
	mainFile.changelog = changelogContent
}
