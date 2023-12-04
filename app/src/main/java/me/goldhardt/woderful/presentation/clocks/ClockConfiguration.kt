package me.goldhardt.woderful.presentation.clocks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.rememberPickerState
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import me.goldhardt.woderful.R
import me.goldhardt.woderful.presentation.component.ConfigurationButton
import me.goldhardt.woderful.presentation.component.RoundText
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
                MinutePicker(
                    pickerState = state,
                    modifier = Modifier.weight(1f)
                )
                ConfigurationButton(
                    onConfirm = {
                        // state.selectedOption is the index selected item
                        val minute = state.selectedOption + 1
                        onConfirm(minute)
                    },
                    icon = Icons.Filled.Check,
                    contentDescription = stringResource(R.string.action_confirm),
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun MinutesAndSecondsTimeConfiguration(
    title: String,
    confirmIcon: ImageVector = Icons.Filled.Check,
    confirmButtonEnabledCondition: (Int, Int) -> Boolean = { _, _ -> true },
    onConfirm: (Int, Int) -> Unit
) {
    val minuteState = rememberPickerState(initialNumberOfOptions = 60)
    val secondState = rememberPickerState(initialNumberOfOptions = DEFAULT_SECONDS_OPTIONS.size)

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
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    MinuteAndSecondPicker(
                        minuteState = minuteState,
                        secondState = secondState,
                    )
                }
                ConfigurationButton(
                    onConfirm = {
                        val minute = minuteState.selectedOption
                        val second = DEFAULT_SECONDS_OPTIONS[secondState.selectedOption]
                        onConfirm(minute, second)
                    },
                    icon = confirmIcon,
                    contentDescription = stringResource(R.string.action_confirm),
                    enabled = confirmButtonEnabledCondition(
                        minuteState.selectedOption,
                        DEFAULT_SECONDS_OPTIONS[secondState.selectedOption]
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

}


@WearPreviewDevices
@Composable
fun MinutesTimeConfigurationPreview() {
    WODerfulTheme {
        MinutesTimeConfiguration(
            title = stringResource(R.string.title_how_long)
        ) {
        }
    }
}

@WearPreviewDevices
@Composable
fun MinutesAndSecondsTimeConfigurationPreview() {
    WODerfulTheme {
        MinutesAndSecondsTimeConfiguration(
            title = stringResource(R.string.title_every)
        ) { _, _ ->
        }
    }
}
