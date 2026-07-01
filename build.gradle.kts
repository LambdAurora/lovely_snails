import com.modrinth.minotaur.dependencies.ModDependency
import dev.lambdaurora.mcdev.api.McVersionLookup
import dev.lambdaurora.mcdev.api.ModUtils
import dev.lambdaurora.mcdev.api.ModVersionDependency
import dev.lambdaurora.mcdev.task.packaging.PackageModrinthTask
import net.darkhax.curseforgegradle.TaskPublishCurseForge

plugins {
	`java-library`
	alias(libs.plugins.loom)
	alias(libs.plugins.lambdamcdev)
	alias(libs.plugins.licenser)
	id("com.modrinth.minotaur").version("2.+")
	id("net.darkhax.curseforgegradle").version("1.1.+")
}

lambdamcdev.namespace.set(project.property("mod_namespace") as String)
base.archivesName.set(lambdamcdev.namespace)

val mcVersion = libs.versions.minecraft.get()
val compatibleMinecraftVersions = listOf<String>()
val VERSION = project.property("mod_version") as String
version = "$VERSION+${McVersionLookup.getVersionTag(mcVersion)}"

val targetJavaVersion = Integer.parseInt(project.property("java_version").toString())

repositories {
	maven {
		name = "Gegy"
		url = uri("https://maven.gegy.dev/releases/")
	}
}

dependencies {
	//to change the versions see the gradle.properties file
	minecraft(libs.minecraft)
	implementation(libs.fabric.loader)

	implementation(libs.fabric.api)
	implementation(libs.yumi.mc.foundation)
	include(libs.yumi.mc.foundation)
}

java {
	sourceCompatibility = JavaVersion.toVersion(targetJavaVersion)
	targetCompatibility = JavaVersion.toVersion(targetJavaVersion)

	withSourcesJar()
}

lambdamcdev {
	manifests {
		fmj {
			val sourcesLink = "https://github.com/LambdAurora/lovely_snails"

			withDescription(project.property("mod_description") as String)
			withAuthors("LambdAurora")
			withContributors("Arathain", "Patbox", "Drex")
			withContact {
				it.withHomepage("https://modrinth.com/mod/lovely_snails")
					.withSources("$sourcesLink.git")
					.withIssues("$sourcesLink/issues")
			}
			withLicense("Lambda License")
			withIcon("assets/${namespace.get()}/icon.png")
			withEnvironment("*")
			withEntrypoints("yumi:init", "dev.lambdaurora.lovely_snails.LovelySnails")
			withEntrypoints("yumi:client_init", "dev.lambdaurora.lovely_snails.client.LovelySnailsClient")
			withAccessWidener("${namespace.get()}.classtweaker")
			withMixins("${namespace.get()}.mixins.json")
			withDepend("fabricloader", ">=${libs.versions.fabric.loader.get()}")
			withDepend("minecraft", project.property("fabric_mc_constraints").toString())
			withDepend("java", ">=$targetJavaVersion")
			withDepend("yumi_mc_core", ">=${libs.versions.yumi.mc.foundation.get()}")
			withDepend("fabric-api", ">=${libs.versions.fabric.api.get()}")
			withModMenu {
				it.withCurseForge("https://www.curseforge.com/minecraft/mc-mods/lovely-snails")
					.withDiscord("https://discord.lambdaurora.dev/")
					.withGitHubReleases("$sourcesLink/releases")
					.withModrinth("https://modrinth.com/mod/lovely_snails")
					.withLink("modmenu.bluesky", "https://bsky.app/profile/lambdaurora.dev")
					.withLink("modmenu.donate", "https://donate.lambdaurora.dev/")
			}
		}
	}

	setupActionsRefCheck()
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
		expand("version" to (inputs.properties["version"] as String))
	}
}

tasks.jar {
	inputs.property("namespace", lambdamcdev.namespace)

	from("LICENSE") {
		rename { "${it}_${inputs.properties["namespace"]}" }
	}
}

license {
	rule(rootProject.file("codeformat/HEADER"))

	include("**/*.java")
}

loom {
	accessWidenerPath = file("src/main/resources/lovely_snails.classtweaker")
}

val packageModrinth = tasks.register<PackageModrinthTask>("packageModrinth") {
	this.group = "publishing"
	this.versionType.set(ModUtils.getVersionType(VERSION, mcVersion))
	this.versionName.set("${project.property("mod_name")} $VERSION (${McVersionLookup.getVersionTag(mcVersion)})")
	this.gameVersions.set(setOf(mcVersion) + compatibleMinecraftVersions)
	this.loaders.set(listOf("fabric", "quilt"))
	this.dependencies.set(
		listOf(
			ModVersionDependency("P7dR8mSH", ModVersionDependency.Type.REQUIRED),
		)
	)
	this.changelog.set(ModUtils.fetchChangelog(project, VERSION))
	this.readme.set(ModUtils.parseReadme(
		project, "https://raw.githubusercontent.com/LambdAurora/lovely_snails/26.2/\$2"
	))
	this.files.setFrom(tasks.jar)
}

modrinth {
	projectId = project.property("modrinth_id") as String
	versionName = "${project.property("mod_name")} $VERSION (${McVersionLookup.getVersionTag(mcVersion)})"
	versionType.set(ModUtils.fetchVersionType(VERSION, mcVersion))
	uploadFile.set(tasks.jar.get())
	loaders.set(listOf("fabric", "quilt"))
	gameVersions.set(setOf(mcVersion) + compatibleMinecraftVersions)
	dependencies.set(
		listOf(
			ModDependency("P7dR8mSH", "required") // Fabric API
		)
	)
	syncBodyFrom.set(
		ModUtils.parseReadme(
			project, "https://raw.githubusercontent.com/LambdAurora/lovely_snails/26.1/\$2"
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

	val mainFile = upload(project.property("curseforge_id"), tasks.jar.get())
	mainFile.releaseType = ModUtils.fetchVersionType(VERSION, mcVersion)
	mainFile.addGameVersion(McVersionLookup.getCurseForgeEquivalent(mcVersion))
	compatibleMinecraftVersions.stream()
		.map { McVersionLookup.getCurseForgeEquivalent(it) }
		.forEach { mainFile.addGameVersion(it) }
	mainFile.addModLoader("Fabric", "Quilt")
	mainFile.addJavaVersion("Java 25")

	mainFile.displayName = "${project.property("mod_name")} $VERSION (${McVersionLookup.getVersionTag(mcVersion)})"
	mainFile.addRequirement("fabric-api")

	mainFile.changelogType = "markdown"
	mainFile.changelog = changelogContent
}
