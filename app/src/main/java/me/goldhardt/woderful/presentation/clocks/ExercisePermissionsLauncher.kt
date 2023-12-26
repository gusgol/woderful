package me.goldhardt.woderful.presentation.clocks

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import kotlinx.coroutines.launch
import me.goldhardt.woderful.R
import me.goldhardt.woderful.presentation.clocks.ExercisePermissions.DEFAULT_EXERCISE_PERMISSIONS
import me.goldhardt.woderful.presentation.theme.WODerfulTheme

/**
 * To do's:
 *  1. TODO If user has to go to the settings to grant permissions, make it automatically go to the workout screen after.
 */

object ExercisePermissions {

    val DEFAULT_EXERCISE_PERMISSIONS = mutableListOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACTIVITY_RECOGNITION,
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            this.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    fun hasPermissions(
        context: Context,
        permissions: List<String> = DEFAULT_EXERCISE_PERMISSIONS,
    ): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}


@Composable
fun ExercisePermissionsLauncher(
    permissions: List<String> = DEFAULT_EXERCISE_PERMISSIONS,
    granted: () -> Unit,
) {

    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    var permissionsGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        permissionsGranted = if (result.all { it.value }) {
            granted()
            true
        } else {
            false
        }
    }

    LaunchedEffect(Unit) {
        launch {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.Warning,
            contentDescription = stringResource(R.string.message_warning),
            tint = MaterialTheme.colors.primary,
            modifier = Modifier
                .size(24.dp)
        )
        Text(
            text = stringResource(R.string.message_permissions),
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp)
        )
        if (!permissionsGranted) {
            CompactChip(
                onClick = {
                    coroutineScope.launch {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", context.packageName, null)
                        context.startActivity(intent)
                    }
                },
                label = {
                    Text("Grant permissions", maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
            )
        }
    }
}

@WearPreviewDevices
@Composable
fun ExercisePermissionsLauncherPreview() {
    WODerfulTheme {
        ExercisePermissionsLauncher {
        }
    }
}