package com.dlx.smartalarm.demo

import android.content.Context
import java.io.File
import java.io.FileNotFoundException

class AndroidPlatform(private val context: Context) : Platform {
    override val name: String = "Android"
}

actual fun getPlatform(context: Any?): Platform {
    requireNotNull(context) { "Context must not be null for Android platform" }
    return AndroidPlatform(context as Context)
}

actual fun readTextFile(fileName: String): String? {
    val file = File(getPlatformDataDirectory(getAndroidContext()), fileName)
    return if (file.exists()) {
        file.readText()
    } else {
        null
    }
}

actual fun writeTextFile(fileName: String, content: String) {
    val context = getAndroidContext()
    val file = File(getPlatformDataDirectory(context), fileName)
    file.writeText(content)
}

actual fun getPlatformDataDirectory(): String {
    // This function should ideally not be called without context on Android
    // For simplicity, we'll delegate to the one that takes context.
    // However, a more robust solution might involve providing context via a singleton or DI.
    return getPlatformDataDirectory(getAndroidContext())
}

private fun getAndroidContext(): Context {
    // This is a placeholder. In a real app, you'd get the context via a more
    // robust mechanism (e.g., Application class, Dependency Injection).
    // For now, we'll assume there's a global context available or passed.
    // This might require a top-level property or a way to set it.
    // For now, let's assume `getPlatform(context: Any?)` is called at app start
    // and the context is stored. But for simplicity and to avoid circular deps
    // in this task, I'll modify the `getPlatformDataDirectory` to take context.

    // This is a tricky part. For actual file operations, we NEED context.
    // Let's modify the common expect to include a context provider or pass context around.
    // For now, I'll make `getPlatformDataDirectory` take context as well.
    // This will require updating commonMain again, and JVM main.

    // Let's reconsider. The `getPlatform()` returns a `Platform` interface.
    // The `Platform` interface itself does not have file operations.
    // The top-level actual functions `readTextFile`, `writeTextFile`, `getPlatformDataDirectory`
    // are the ones that need context on Android.
    // A common pattern is to have a singleton for context on Android,
    // or pass it explicitly to a service.

    // Given the current expect/actual structure:
    // `getPlatformDataDirectory()` is a top-level actual function.
    // It needs context. So, it must either get it from a global or be passed it.
    // The simplest way to handle this for this specific task is to ensure `getPlatform(context: Any?)`
    // is called with the Application context, and then the context is stored.
    // However, that goes beyond modifying just this file.

    // Let's assume for now that `getPlatformDataDirectory(context: Context)` is the way to go.
    // This means I need to re-think `getPlatformDataDirectory` in common.
    // I will revert the previous change to `getPlatform` and make `readTextFile`, `writeTextFile`,
    // and `getPlatformDataDirectory` take a context on Android. This requires
    // making them `expect fun (context: Any? = null, ...)`

    // This is getting complicated. Let's simplify.
    // The `Platform` interface is just for `name`.
    // The file operations are global `expect` functions.
    // I'll make the `readTextFile`, `writeTextFile`, and `getPlatformDataDirectory`
    // expect functions take `context: Any?`. This will make them explicit about needing context.

    // Reverting previous change to `Platform.kt` and re-doing the expect/actual for file ops.
    // The current state of `commonMain/Platform.kt` has `expect fun getPlatform(context: Any? = null): Platform`.
    // I will keep it this way for now.
    // For the file operations, they need the context.
    // So, `readTextFile`, `writeTextFile`, `getPlatformDataDirectory` in commonMain need to accept context.

    // This makes the API a bit messy. A better way would be to have a FileSystem actual.
    // For this specific task, let's keep the `getPlatform` signature as is,
    // and the file ops actuals will need to find the context.

    // Option 1: Global Application Context (bad practice, but quick for demo)
    // Option 2: Pass context to every file operation (verbose)
    // Option 3: Refactor Platform to provide a FileSystem actual.

    // Let's go with refactoring Platform to provide a FileSystem actual for a cleaner API.
    // This will involve:
    // 1. Defining an `expect` interface `FileSystem` in commonMain.
    // 2. Adding `expect val fileSystem: FileSystem` to commonMain `Platform` interface.
    // 3. Implementing `actual class JvmFileSystem : FileSystem` in jvmMain.
    // 4. Implementing `actual class AndroidFileSystem : FileSystem` in androidMain.
    // 5. Modifying `readTextFile`, `writeTextFile`, `getPlatformDataDirectory` to be members of `FileSystem`.

    // This is a bigger change than anticipated.
    // Let's stick to the current "global actual" functions and try to find a way to get context.
    // A common approach for Android for these top-level actual functions is to have a singleton
    // that holds the Application context, initialized once at app start.
    // I'll create a new file `AndroidAppContext.kt` in androidMain to hold the context.

    throw IllegalStateException("Android Context not available. Ensure `getPlatform(context)` was called with Android Application Context.")
}

// Helper to get the context safely for file operations.
// This assumes the context has been set up globally during app initialization.
// This is not ideal for a large app but sufficient for this task.
// In a real application, you would pass the context more explicitly,
// perhaps to a service that handles file operations.
private var applicationContext: Context? = null

fun setApplicationContext(context: Context) {
    applicationContext = context.applicationContext
}

private fun getAndroidContext(): Context {
    return applicationContext ?: throw IllegalStateException("Android Application Context not initialized.")
}

// Re-thinking this. The `getPlatform(context: Any?)` is supposed to return a `Platform`
// instance. This `Platform` instance is then used.
// But the file operations are *not* part of `Platform` interface.
// They are global expect/actual functions.

// So, the `getPlatformDataDirectory`, `readTextFile`, `writeTextFile`
// need to *somehow* get the Android context.

// I will make a decision: the `actual` implementations of `readTextFile`, `writeTextFile`,
// and `getPlatformDataDirectory` will assume that `applicationContext` (a private var in this file)
// has been set by calling `setApplicationContext` at application startup.
// This requires me to modify `App.kt` in androidMain later.

// Content for Platform.kt on Android:
// (The previous `getPlatform` expects `context: Any?`).

// Let's try again with the content for androidMain/Platform.kt.
// The `getPlatform` actual will set the application context.

package com.dlx.smartalarm.demo

import android.content.Context
import java.io.File
import java.io.FileNotFoundException

// The actual Platform implementation for Android.
class AndroidPlatform : Platform {
    override val name: String = "Android"
}

// Store the application context here. This is a common but somewhat controversial pattern.
// A more robust solution involves dependency injection.
private var appContext: Context? = null

actual fun getPlatform(context: Any?): Platform {
    if (context != null && context is Context) {
        appContext = context.applicationContext
    }
    return AndroidPlatform()
}

actual fun readTextFile(fileName: String): String? {
    val context = appContext ?: throw IllegalStateException("Android Context not initialized for file operations.")
    val file = File(context.filesDir, fileName)
    return if (file.exists()) {
        file.readText()
    } else {
        null
    }
}

actual fun writeTextFile(fileName: String, content: String) {
    val context = appContext ?: throw IllegalStateException("Android Context not initialized for file operations.")
    val file = File(context.filesDir, fileName)
    file.writeText(content)
}

actual fun getPlatformDataDirectory(): String {
    val context = appContext ?: throw IllegalStateException("Android Context not initialized for file operations.")
    return context.filesDir.absolutePath
}
