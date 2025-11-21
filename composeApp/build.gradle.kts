import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.file.RegularFileProperty

val versionPropsFile = file("version.properties")
val versionProps = Properties().apply {
	if (versionPropsFile.exists()) {
		load(versionPropsFile.inputStream())
	} else {
		setProperty("VERSION_CODE", "1")
		store(versionPropsFile.outputStream(), null)
	}
}

val currentCode = versionProps["VERSION_CODE"].toString().toInt()
val newCode = currentCode + 1
versionProps["VERSION_CODE"] = newCode.toString()
versionProps.store(versionPropsFile.outputStream(), null)


abstract class GenerateGitVersionCodeTask : DefaultTask() {
	@get:OutputFile
	abstract val outputFile: RegularFileProperty

	@TaskAction
	fun generate() {
		// 调用 git rev-list --count HEAD
		val process = ProcessBuilder("git", "rev-list", "--count", "HEAD")
			.directory(project.rootDir)
			.redirectErrorStream(true)
			.start()

		val count = process.inputStream.bufferedReader().use { it.readText() }.trim()

		if (count.isNullOrEmpty()) {
			throw RuntimeException("Cannot get git commit count")
		}

		// 输出文件不存在会自动创建
		val outFile = outputFile.get().asFile
		outFile.parentFile.mkdirs()
		outFile.writeText(count)
	}
}

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
			isStatic = true
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

// --------------------------------------------------------
// 2. ★ 在 android {} 之前，注册任务 + 输出文件
// --------------------------------------------------------
//val gitVersionCodeFile = file("version.properties")
val gitVersionCodeFile = layout.buildDirectory.file("git/versionCode.txt")

val generateGitVersionCode = tasks.register<GenerateGitVersionCodeTask>("generateGitVersionCode") {
	outputFile.set(gitVersionCodeFile)
}

android {
	namespace = "com.dlx.smartalarm.demo"
	compileSdk = libs.versions.android.compileSdk.get().toInt()

	defaultConfig {
		applicationId = "com.dlx.smartalarm.demo"
		minSdk = libs.versions.android.minSdk.get().toInt()
		targetSdk = libs.versions.android.targetSdk.get().toInt()

		val versionCodeFile = gitVersionCodeFile.get().asFile
		val code = if (versionCodeFile.exists()) {
			val text: String = versionCodeFile.readText()
			if (text.isEmpty())
			{
				1
			}
			else {
				text.toInt()
			}
		} else {
			1   // 第一次构建时用 1 顶着
		}


		versionCode = code
		versionName = "1.0.%05d".format(code)
//		versionCode = newCode
//		val formatted = String.format("%05d", newCode)
//		versionName = "1.0.$formatted"
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

dependencies {
	debugImplementation(compose.uiTooling)
}

compose.desktop {
	application {
		mainClass = "com.dlx.smartalarm.demo.MainKt"

		nativeDistributions {
			targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
			packageName = "com.dlx.smartalarm.demo"
			val formatted = String.format("%05d", newCode)
			packageVersion = "1.0.${formatted}"
		}
	}
}

// --------------------------------------------------------
// 4. ★ 在文件末尾，让编译前执行任务
// --------------------------------------------------------
tasks.named("preBuild") {
	dependsOn(generateGitVersionCode)
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