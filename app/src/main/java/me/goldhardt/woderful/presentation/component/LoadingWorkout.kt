package me.goldhardt.woderful.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import me.goldhardt.woderful.R

@Composable
fun LoadingWorkout() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.title_loading),
            style = MaterialTheme.typography.body1
        )
    }
}