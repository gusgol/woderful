package me.goldhardt.woderful.presentation.clocks

import android.view.MotionEvent
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Picker
import androidx.wear.compose.material.PickerDefaults
import androidx.wear.compose.material.PickerScope
import androidx.wear.compose.material.PickerState
import androidx.wear.compose.material.Text

/**
 * A picker that allows the user to select a minute.
 *
 * @param pickerState The state of the picker.
 * @param modifier The modifier to be applied to the picker.
 * @param readOnly Whether the picker is read-only.
 *
 * @see Holorogist library.
 */
@Composable
fun MinutePicker(
    pickerState: PickerState,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
) {
    val textStyle = MaterialTheme.typography.display3
    val focusRequester = remember { FocusRequester() }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        PickerWithRSB(
            readOnly = readOnly,
            state = pickerState,
            focusRequester = focusRequester,
            modifier = Modifier.size(40.dp, 100.dp),
            contentDescription = "%02d".format(pickerState.selectedOption),
            onSelected = {}
        ) { index: Int ->
            val minute = index + 1
            TimePiece(
                selected = pickerState.selectedOption == index,
                text = "%02d".format(minute),
                style = textStyle,
                onSelected = { }
            )
        }
        LaunchedEffect(this) {
                focusRequester.requestFocus()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun TimePiece(
    selected: Boolean,
    onSelected: () -> Unit,
    text: String,
    style: TextStyle
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val modifier = Modifier
            .align(Alignment.Center)
            .wrapContentSize()
        Text(
            text = text,
            maxLines = 1,
            style = style,
            color = if (selected) MaterialTheme.colors.secondary
            else MaterialTheme.colors.onBackground,
            modifier = if (selected) {
                modifier
            } else {
                modifier.pointerInteropFilter {
                    if (it.action == MotionEvent.ACTION_DOWN) onSelected()
                    true
                }
            }
        )
    }
}

@Composable
internal fun PickerWithRSB(
    state: PickerState,
    readOnly: Boolean,
    modifier: Modifier,
    focusRequester: FocusRequester,
    contentDescription: String?,
    readOnlyLabel: @Composable (BoxScope.() -> Unit)? = null,
    flingBehavior: FlingBehavior = PickerDefaults.flingBehavior(state = state),
    onSelected: () -> Unit = {},
    option: @Composable PickerScope.(optionIndex: Int) -> Unit
) {
    Picker(
        state = state,
        contentDescription = contentDescription,
        onSelected = onSelected,
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable(),
        flingBehavior = flingBehavior,
        readOnly = readOnly,
        readOnlyLabel = readOnlyLabel,
        option = option
    )
}


