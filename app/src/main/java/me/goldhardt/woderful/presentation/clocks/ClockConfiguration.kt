package me.goldhardt.woderful.presentation.clocks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
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
fun TimeConfiguration(
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


@Preview(
    device = Devices.WEAR_OS_SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
fun TimeConfigurationPreview() {
    WODerfulTheme {
        TimeConfiguration(
            title = stringResource(R.string.title_how_long)
        ) {
        }
    }
}
