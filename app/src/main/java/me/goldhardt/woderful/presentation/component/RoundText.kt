package me.goldhardt.woderful.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.wear.compose.foundation.CurvedLayout
import androidx.wear.compose.foundation.curvedRow
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.curvedText

@Composable
fun RoundText(
    text: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = TimeTextDefaults.ContentPadding,
) {
    if (LocalConfiguration.current.isScreenRound) {
        CurvedLayout {
            curvedRow {
                curvedText(
                    text = text,
                )
            }
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                maxLines = 1,
            )
        }
    }
}