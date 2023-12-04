/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package me.goldhardt.woderful.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.navigation.currentBackStackEntryAsState
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import dagger.hilt.android.AndroidEntryPoint
import me.goldhardt.woderful.presentation.navigation.MainNavigation
import me.goldhardt.woderful.presentation.navigation.WODerfulScreens.HOME
import me.goldhardt.woderful.presentation.theme.WODerfulTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    val listState = rememberScalingLazyListState()
    val navController = rememberSwipeDismissableNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    WODerfulTheme {
        Scaffold(
            timeText = {
                if (!listState.isScrollInProgress && currentDestination?.route == HOME) {
                    TimeText()
                }
            },
            vignette = {
                Vignette(vignettePosition = VignettePosition.TopAndBottom)
            },
            positionIndicator = {
                PositionIndicator(
                    scalingLazyListState = listState
                )
            }
        ) {
            MainNavigation(
                listState = listState,
                navController = navController
            )
        }
    }
}



@WearPreviewDevices
@Composable
fun DefaultPreview() {
    WearApp()
}