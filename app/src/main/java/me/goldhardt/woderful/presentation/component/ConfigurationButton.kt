package me.goldhardt.woderful.presentation.component

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon

@Composable
fun ConfigurationButton(
    onConfirm: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean = true
) {
    Button(
        onClick = onConfirm,
        modifier = Modifier.size(40.dp),
        enabled = enabled,
    ) {
        Icon(
            imageVector = icon,
            contentDescription =  contentDescription,
            modifier = Modifier
                .size(24.dp)
                .wrapContentSize(align = Alignment.Center)
        )
    }
}