package me.goldhardt.woderful.presentation.clocks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.CurvedLayout
import androidx.wear.compose.foundation.curvedRow
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.curvedText
import androidx.wear.compose.material.rememberPickerState
import me.goldhardt.woderful.R
import me.goldhardt.woderful.presentation.theme.WODerfulTheme

/**
 * Index of initial selected item in the picker.
 * For minutes picker, this is the equivalent of 12 minutes (11 + 1).
 */
private const val DEFAULT_SELECTED_INDEX = 11

/**
 * Range of minutes to show in the picker.
 */
private val DEFAULT_MINUTES_RANGE = 1..60

@Composable
fun MinutesTimeConfiguration(
    range: List<Int> = DEFAULT_MINUTES_RANGE.toList(),
    title: String,
    onConfirm: (Int) -> Unit
) {
    val state = rememberPickerState(
        initialNumberOfOptions = range.size,
        initiallySelectedOption = DEFAULT_SELECTED_INDEX
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
            )
            MinutePicker(
                pickerState = state,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    // state.selectedOption is the index selected item
                    val minute = state.selectedOption + 1
                    onConfirm(minute)
                },
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription =  stringResource(R.string.action_confirm),
                    modifier = Modifier
                        .size(24.dp)
                        .wrapContentSize(align = Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun MinutesAndSecondsTimeConfiguration(
    title: String,
    confirmIcon: ImageVector = Icons.Filled.Check,
    onConfirm: (Int, Int) -> Unit
) {
    val minuteState = rememberPickerState(initialNumberOfOptions = 60)
    val secondState = rememberPickerState(initialNumberOfOptions = 60)

    Scaffold(
        timeText = {
            RoundText(title)
        }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Box(modifier = Modifier.weight(1f)) {
                    MinuteAndSecondPicker(
                        minuteState = minuteState,
                        secondState = secondState,
                    )
                }
                Button(
                    onClick = {
                        val minute = minuteState.selectedOption
                        val second = secondState.selectedOption
                        onConfirm(minute, second)
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = confirmIcon,
                        contentDescription =  stringResource(R.string.action_confirm),
                        modifier = Modifier
                            .size(24.dp)
                            .wrapContentSize(align = Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

}


@Preview(
    device = Devices.WEAR_OS_SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
fun MinutesTimeConfigurationPreview() {
    WODerfulTheme {
        MinutesTimeConfiguration(
            title = stringResource(R.string.title_how_long)
        ) {
        }
    }
}

@Preview(
    device = Devices.WEAR_OS_SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
fun MinutesAndSecondsTimeConfigurationPreview() {
    WODerfulTheme {
        MinutesAndSecondsTimeConfiguration(
            title = stringResource(R.string.title_every)
        ) { _, _ ->
        }
    }
}

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
