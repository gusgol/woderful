package me.goldhardt.woderful.presentation.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FrontHand
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import kotlinx.coroutines.launch
import me.goldhardt.woderful.R

/**
 * Stop page for [HorizontalPager].
 */
internal const val STOP_PAGE = 0

/**
 * The initial page of the [HorizontalPager].
 */
internal const val INITIAL_PAGE = 1

/**
 * The number of pages in the [HorizontalPager].
 */
internal const val PAGE_COUNT = 2

/**
 * The number simultaneous of pages in the [HorizontalPager].
 */
internal const val NUMBER_OF_PAGES = 1

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StopWorkoutContainer(
    onConfirm: () -> Unit,
    content: @Composable () -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = INITIAL_PAGE,
        pageCount = { PAGE_COUNT }
    )
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        coroutineScope.launch {
            pagerState.scrollToPage(STOP_PAGE)
        }
    }

    HorizontalPager(
        state = pagerState, modifier = Modifier.fillMaxSize(),
        beyondBoundsPageCount = NUMBER_OF_PAGES
    ) { page ->
        if (page == 0) {
            ConfirmationScreen(onConfirm) {
                coroutineScope.launch {
                    pagerState.scrollToPage(INITIAL_PAGE)
                }
            }
        } else {
            content()
        }
    }
}

@Composable
internal fun ConfirmationScreen(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Alert(
        icon = {
            Icon(
                Icons.Default.FrontHand,
                contentDescription = stringResource(R.string.title_end_workout),
                modifier = Modifier
                    .size(24.dp)
                    .wrapContentSize(align = Alignment.Center),
            )
        },
        title = {
            Text(
                text = stringResource(R.string.title_end_workout),
                textAlign = TextAlign.Center
            )
        },
        negativeButton = {
            Button(
                colors = ButtonDefaults.secondaryButtonColors(),
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.title_no))
            }
        },
        positiveButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.title_yes))
            }
        },
        contentPadding =
        PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 32.dp),
    ) {
        Text(
            text = stringResource(R.string.title_end_confirm_desc),
            textAlign = TextAlign.Center
        )
    }
}

