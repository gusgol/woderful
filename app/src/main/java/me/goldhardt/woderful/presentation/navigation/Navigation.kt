package me.goldhardt.woderful.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberPickerState
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import com.google.android.horologist.composables.TimePicker
import me.goldhardt.woderful.data.ClockType
import me.goldhardt.woderful.presentation.clocks.MinutePicker
import me.goldhardt.woderful.presentation.clocks.TimeConfiguration
import me.goldhardt.woderful.presentation.clocks.amrap.AmrapScreen
import me.goldhardt.woderful.presentation.home.HomeScreen
import me.goldhardt.woderful.presentation.navigation.WODerfulScreens.AMRAP
import me.goldhardt.woderful.presentation.navigation.WODerfulScreens.EMOM
import me.goldhardt.woderful.presentation.navigation.WODerfulScreens.HOME

object WODerfulScreens {
    const val HOME = "home"

    // Clock types
    private const val CLOCK = "clock"
    const val AMRAP = "$CLOCK/amrap"
    const val EMOM = "$CLOCK/emom"

}

@Composable
fun MainNavigation(
    listState: ScalingLazyListState,
    navController: NavHostController
) {
    SwipeDismissableNavHost(
        navController = navController,
        startDestination = HOME
    ) {
        composable(HOME) {
            HomeScreen(listState = listState) { type ->
                when(type) {
                    ClockType.AMRAP -> navController.navigate(AMRAP)
                    ClockType.EMOM -> navController.navigate(EMOM)
                    else -> {
                    }
                }
            }
        }
        composable(AMRAP) {
            AmrapScreen()
        }
        composable(EMOM) {
            val minuteState = rememberPickerState(
                initialNumberOfOptions = 60,
                initiallySelectedOption = 12
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                MinutePicker(minuteState)
            }
        }
    }
}

