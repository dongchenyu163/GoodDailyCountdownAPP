import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import java.io.File
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.provider.Provider

val versionMajor = providers.gradleProperty("versionMajor").orElse("1").get().toInt()
val versionMinor = providers.gradleProperty("versionMinor").orElse("0").get().toInt()

abstract class GitCommitCountSource : ValueSource<Int, GitCommitCountSource.Parameters> {
	interface Parameters : ValueSourceParameters {
		@get:Input
		val ref: org.gradle.api.provider.Property<String>
		@get:InputDirectory
		val gitDir: DirectoryProperty
	}

	override fun obtain(): Int {
		val process = ProcessBuilder("git", "rev-list", "--count", parameters.ref.get())
			.directory(parameters.gitDir.get().asFile)
			.redirectErrorStream(true)
			.start()
		val text = process.inputStream.bufferedReader().use { it.readText().trim() }
		return text.toIntOrNull() ?: 1
	}
}

val buildIdProvider: Provider<Int> = providers.of(GitCommitCountSource::class) {
	parameters.ref.set("master")
	parameters.gitDir.set(layout.projectDirectory)
}
val buildId = buildIdProvider.orElse(1).get()
val versionNameString = "$versionMajor.$versionMinor.$buildId"
val buildTimestamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now())

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.androidApplication)
	alias(libs.plugins.composeMultiplatform)
	alias(libs.plugins.composeCompiler)
	alias(libs.plugins.composeHotReload)
	alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.moko.resources)
}

kotlin {
	androidTarget {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_11)
		}
	}

	listOf(
		iosArm64(),
		iosSimulatorArm64()
	).forEach { iosTarget ->
		iosTarget.binaries.framework {
			baseName = "ComposeApp"
			// Use dynamic framework so iOS can load the associated resource bundle (moko-resources)
			isStatic = false
			// Unique bundleId for the shared framework (must differ from the app's bundle id)
			freeCompilerArgs += listOf("-Xbinary=bundleId=com.dlx.smartalarm.demo.framework")
		}
	}

	jvm()

	js {
		browser()
		binaries.executable()
	}

	@OptIn(ExperimentalWasmDsl::class)
	wasmJs {
		browser()
		binaries.executable()
	}

		sourceSets {
			androidMain.dependencies {
				implementation(compose.preview)
				implementation(libs.androidx.activity.compose)
				implementation(libs.androidx.core.ktx)
				implementation(libs.coil.compose)
				implementation(libs.coil.svg)
			}
			commonMain.dependencies {
				implementation(compose.runtime)
				implementation(compose.foundation)
				implementation(compose.material3)
				// 为 SwipeToDismiss 等组件引入 material（非material3)
				implementation(compose.material)
				implementation(compose.ui)
				implementation(compose.components.resources)
				implementation(compose.components.uiToolingPreview)
				implementation(libs.androidx.lifecycle.viewmodelCompose)
				implementation(libs.androidx.lifecycle.runtimeCompose)
			implementation(libs.kotlinx.datetime)
			implementation(libs.kotlinx.serialization.json)
                api(libs.moko.resources)
                api(libs.moko.resources.compose)
                implementation(libs.compottie)
                implementation(libs.compottie.resources)
                implementation(libs.compottie.dot)
                implementation(libs.compottie.network)
		}
		commonTest.dependencies {
			implementation(libs.kotlin.test)
		}
		jvmMain.dependencies {
			implementation(compose.desktop.currentOs)
			implementation(libs.kotlinx.coroutinesSwing)
			implementation(libs.kotlinx.datetime)
		}
		jsMain.dependencies {
			implementation(libs.kotlinx.datetime)
		}
		wasmJsMain.dependencies {
			implementation(libs.kotlinx.datetime)
		}
	}
}

android {
	namespace = "com.dlx.smartalarm.demo"
	compileSdk = libs.versions.android.compileSdk.get().toInt()

	defaultConfig {
		applicationId = "com.dlx.smartalarm.demo"
		minSdk = libs.versions.android.minSdk.get().toInt()
		targetSdk = libs.versions.android.targetSdk.get().toInt()

		versionCode = buildId
		versionName = versionNameString
	}
	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
	buildTypes {
		getByName("release") {
			isMinifyEnabled = false
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
}

androidComponents {
	onVariants(selector().all()) { /* keep for future new API hooks */ }
}

android.applicationVariants.configureEach {
	outputs.forEach { output ->
		val outputImpl = output as BaseVariantOutputImpl
		val ext = outputImpl.outputFile.extension.ifBlank { "apk" }
		outputImpl.outputFileName = "demo-${name}-${versionNameString}-$buildTimestamp.$ext"
	}
}

val renameBundleRelease = tasks.register<Copy>("renameBundleRelease") {
	// Will be enabled only if bundleRelease exists
	enabled = false
	group = "build"
	description = "Copy and rename release AAB with version and timestamp"
	from(layout.buildDirectory.dir("outputs/bundle/release"))
	include("*.aab")
	into(layout.buildDirectory.dir("outputs/bundle/release/renamed"))
	duplicatesStrategy = DuplicatesStrategy.INCLUDE
	val targetName = providers.provider { "demo-release-${versionNameString}-$buildTimestamp.aab" }
	rename { targetName.get() }
}

gradle.projectsEvaluated {
	tasks.findByName("bundleRelease")?.let { bundle ->
		renameBundleRelease.configure {
			enabled = true
			dependsOn(bundle)
		}
		bundle.finalizedBy(renameBundleRelease)
	}
}

dependencies {
	debugImplementation(compose.uiTooling)
}

compose.desktop {
	application {
		mainClass = "com.dlx.smartalarm.demo.MainKt"

		nativeDistributions {
			targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
			packageName = "com.dlx.smartalarm.demo"
			packageVersion = versionNameString
		}
	}
}

multiplatformResources {
    resourcesPackage.set("com.dlx.smartalarm.demo")
}

// --------------------------------------------------------
// Fix for WasmJs Moko Resources
// --------------------------------------------------------
val copyWasmResourcesProd = tasks.register<Copy>("copyWasmResourcesProd") {
    from(layout.buildDirectory.dir("classes/kotlin/wasmJs/main/default/resources/moko-resources-js"))
    into(layout.buildDirectory.dir("dist/wasmJs/productionExecutable"))
}

tasks.named("wasmJsBrowserDistribution") {
    finalizedBy(copyWasmResourcesProd)
}

val copyWasmResourcesDev = tasks.register<Copy>("copyWasmResourcesDev") {
    from(layout.buildDirectory.dir("classes/kotlin/wasmJs/main/default/resources/moko-resources-js"))
    into(layout.buildDirectory.dir("dist/wasmJs/developmentExecutable"))
}

tasks.findByName("wasmJsBrowserDevelopmentExecutableDistribution")?.let {
    it.finalizedBy(copyWasmResourcesDev)
}

val copyWasmResourcesToPackages = tasks.register<Copy>("copyWasmResourcesToPackages") {
    from(layout.buildDirectory.dir("classes/kotlin/wasmJs/main/default/resources/moko-resources-js"))
    into(rootProject.layout.buildDirectory.dir("wasm/packages/demo-composeApp/kotlin"))
}

tasks.named("wasmJsDevelopmentExecutableCompileSync") {
    finalizedBy(copyWasmResourcesToPackages)
}

tasks.named("wasmJsProductionExecutableCompileSync") {
    finalizedBy(copyWasmResourcesToPackages)
}

tasks.named("wasmJsBrowserProductionWebpack") {
    dependsOn(copyWasmResourcesToPackages)
}

tasks.named("wasmJsBrowserDevelopmentWebpack") {
    dependsOn(copyWasmResourcesToPackages)
}

tasks.named("wasmJsBrowserDevelopmentRun") {
    dependsOn(copyWasmResourcesToPackages)
}

tasks.named("wasmJsBrowserProductionRun") {
    dependsOn(copyWasmResourcesToPackages)
}
