import com.modrinth.minotaur.dependencies.ModDependency
import dev.lambdaurora.mcdev.api.McVersionLookup
import dev.lambdaurora.mcdev.api.ModUtils
import dev.lambdaurora.mcdev.api.ModVersionDependency
import dev.lambdaurora.mcdev.task.packaging.PackageModrinthTask
import net.darkhax.curseforgegradle.TaskPublishCurseForge

plugins {
	id("fabric-loom").version("1.14.+")
	id("dev.lambdaurora.mcdev").version("1.8.+")
	id("dev.yumi.gradle.licenser").version("2.+")
	id("com.modrinth.minotaur").version("2.+")
	id("net.darkhax.curseforgegradle").version("1.1.+")
}

val baseVersion = project.property("mod_version").toString()
val modNamespace = project.property("mod_namespace").toString()
val mcVersion = libs.versions.minecraft.get()
version = "$baseVersion+$mcVersion"
base.archivesName.set(modNamespace)

val javaVersion = Integer.parseInt(project.property("java_version").toString())

val compatibleMinecraftVersions = listOf("1.21.11")

repositories {
	maven {
		name = "Gegy"
		url = uri("https://maven.gegy.dev/releases/")
	}
}

dependencies {
	//to change the versions see the gradle.properties file
	minecraft(libs.minecraft)
	mappings(loom.officialMojangMappings())
	modImplementation(libs.fabric.loader)

	modImplementation(libs.fabric.api)
}

java {
	sourceCompatibility = JavaVersion.toVersion(javaVersion)
	targetCompatibility = JavaVersion.toVersion(javaVersion)

	withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.isDeprecation = true
	options.isIncremental = true
	options.release.set(javaVersion)
}

tasks.processResources {
	inputs.property("version", project.version)

	filesMatching("fabric.mod.json") {
		expand("version" to (inputs.properties["version"] as String))
	}
}

tasks.jar {
	inputs.property("namespace", modNamespace)

	from("LICENSE") {
		rename { "${it}_${inputs.properties["namespace"]}" }
	}
}

license {
	rule(rootProject.file("codeformat/HEADER"))
}

val packageModrinth by tasks.registering(PackageModrinthTask::class) {
	this.group = "publishing"
	this.versionType.set(ModUtils.getVersionType(baseVersion, mcVersion))
	this.versionName.set("${project.property("mod_name")} $baseVersion (${McVersionLookup.getVersionTag(mcVersion)})")
	this.gameVersions.set(listOf(mcVersion) + compatibleMinecraftVersions)
	this.loaders.set(listOf("fabric", "quilt"))
	this.dependencies.set(
		listOf(
			ModVersionDependency("P7dR8mSH", ModVersionDependency.Type.REQUIRED),
		)
	)
	this.changelog.set(ModUtils.fetchChangelog(project, baseVersion))
	this.readme.set(ModUtils.parseReadme(
		project, "https://raw.githubusercontent.com/LambdAurora/lovely_snails/1.21.11/\$2"
	))
	this.files.setFrom(tasks.remapJar)
}

modrinth {
	projectId = project.property("modrinth_id") as String
	versionName = "${project.property("mod_name")} $baseVersion (${McVersionLookup.getVersionTag(mcVersion)})"
	versionType.set(ModUtils.fetchVersionType(baseVersion, mcVersion))
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
			project, "https://raw.githubusercontent.com/LambdAurora/lovely_snails/1.21.11/\$2"
		)
	)

	// Changelog fetching
	val changelogContent = ModUtils.fetchChangelog(project, baseVersion)

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
	var changelogContent = ModUtils.fetchChangelog(project, baseVersion)

	if (changelogContent != null) {
		changelogContent = "Changelog:\n\n${changelogContent}"
	} else {
		this.isEnabled = false
		return@register
	}

	val mainFile = upload(project.property("curseforge_id"), tasks.remapJar.get())
	mainFile.releaseType = ModUtils.fetchVersionType(baseVersion, mcVersion)
	mainFile.addGameVersion(McVersionLookup.getCurseForgeEquivalent(mcVersion))
	compatibleMinecraftVersions.stream()
		.map { McVersionLookup.getCurseForgeEquivalent(it) }
		.forEach { mainFile.addGameVersion(it) }
	mainFile.addModLoader("Fabric", "Quilt")
	mainFile.addJavaVersion("Java 21", "Java 22")

	mainFile.displayName = "${project.property("mod_name")} $baseVersion (${McVersionLookup.getVersionTag(mcVersion)})"
	mainFile.addRequirement("fabric-api")

	mainFile.changelogType = "markdown"
	mainFile.changelog = changelogContent
}
