package com.dlx.smartalarm.demo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		enableEdgeToEdge()
		super.onCreate(savedInstanceState)

		ImagePicker.register(this)

		requestNotificationPermissionIfNeeded()

		gIsInitLoad = false
		setContent {
			App()
		}
	}

	private fun requestNotificationPermissionIfNeeded() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			val permission = Manifest.permission.POST_NOTIFICATIONS
			if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(this, arrayOf(permission), NOTIFICATION_PERMISSION_REQUEST_CODE)
			}
		}
	}

	companion object {
		private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
	}
}

@Preview
@Composable
fun AppAndroidPreview() {
	App()
}