package me.goldhardt.woderful.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

@Composable
fun History() {
    Box(modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colors.primary)) {
        Text(text = "History")
    }
}