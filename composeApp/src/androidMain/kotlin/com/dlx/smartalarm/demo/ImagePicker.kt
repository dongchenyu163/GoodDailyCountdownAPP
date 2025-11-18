package com.dlx.smartalarm.demo

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

object ImagePicker {

    private var activityRef: WeakReference<ComponentActivity>? = null
    private var deferred: CompletableDeferred<Uri?>? = null
    private var getContent: ActivityResultLauncher<String>? = null

    fun register(activity: ComponentActivity) {
        activityRef = WeakReference(activity)
        getContent = activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            deferred?.complete(uri)
        }
    }

    suspend fun pickImage(): PickedImage? {
        val activity = activityRef?.get() ?: return null
        val launcher = getContent ?: return null
        deferred = CompletableDeferred()
        launcher.launch("image/*")
        val uri = deferred?.await() ?: return null
        return readImageFromUri(activity, uri)
    }

    private suspend fun readImageFromUri(activity: ComponentActivity, uri: Uri): PickedImage? = withContext(Dispatchers.IO) {
        try {
            val inputStream = activity.contentResolver.openInputStream(uri) ?: return@withContext null
            val bytes = inputStream.readBytes()
            inputStream.close()
            val mimeType = activity.contentResolver.getType(uri)
            val fileName = getFileName(activity, uri)
            PickedImage(bytes = bytes, fileName = fileName, mimeType = mimeType)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(activity: ComponentActivity, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = activity.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        result = cursor.getString(displayNameIndex)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                if (cut != null) {
                    result = result.substring(cut + 1)
                }
            }
        }
        return result
    }
}
