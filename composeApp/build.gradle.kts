import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

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

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.androidApplication)
	alias(libs.plugins.composeMultiplatform)
	alias(libs.plugins.composeCompiler)
	alias(libs.plugins.composeHotReload)
	alias(libs.plugins.kotlinSerialization)
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
				// 为 SwipeToDismiss 等组件引入 material（非material3）
				implementation(compose.material)
				implementation(compose.ui)
				implementation(compose.components.resources)
				implementation(compose.components.uiToolingPreview)
				implementation(libs.androidx.lifecycle.viewmodelCompose)
				implementation(libs.androidx.lifecycle.runtimeCompose)
			implementation(libs.kotlinx.datetime)
			implementation(libs.kotlinx.serialization.json)
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
		versionCode = newCode
		val formatted = String.format("%05d", newCode)
		versionName = "1.0.$formatted"
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
