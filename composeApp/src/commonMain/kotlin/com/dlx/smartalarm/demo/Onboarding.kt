package com.dlx.smartalarm.demo

import com.dlx.smartalarm.demo.MR
import dev.icerock.moko.resources.compose.stringResource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeScreen(onNext: () -> Unit) {
    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(MR.strings.welcome_to_app), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(MR.strings.app_tagline))
            Spacer(Modifier.height(24.dp))
            Button(onClick = onNext) { Text(stringResource(MR.strings.next_step)) }
        }
    }
}

@Composable
fun PermissionsScreen(onGrant: () -> Unit) {
    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(MR.strings.permission_request_message), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onGrant) { Text(stringResource(MR.strings.grant_permission)) }
        }
    }
}
