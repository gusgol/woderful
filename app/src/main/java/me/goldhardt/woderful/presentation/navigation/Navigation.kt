package me.goldhardt.woderful.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import me.goldhardt.woderful.data.model.ClockType
import me.goldhardt.woderful.presentation.clocks.amrap.AmrapScreen
import me.goldhardt.woderful.presentation.clocks.emom.EmomScreen
import me.goldhardt.woderful.presentation.clocks.fortime.ForTimeScreen
import me.goldhardt.woderful.presentation.clocks.tabata.TabataScreen
import me.goldhardt.woderful.presentation.history.History
import me.goldhardt.woderful.presentation.home.HomeScreen
import me.goldhardt.woderful.presentation.navigation.WODerfulScreens.AMRAP
import me.goldhardt.woderful.presentation.navigation.WODerfulScreens.EMOM
import me.goldhardt.woderful.presentation.navigation.WODerfulScreens.FOR_TIME
import me.goldhardt.woderful.presentation.navigation.WODerfulScreens.HISTORY
import me.goldhardt.woderful.presentation.navigation.WODerfulScreens.HOME
import me.goldhardt.woderful.presentation.navigation.WODerfulScreens.TABATA

object WODerfulScreens {
    const val HOME = "home"

    // Clock types
    private const val CLOCK = "clock"
    const val AMRAP = "$CLOCK/amrap"
    const val EMOM = "$CLOCK/emom"
    const val TABATA = "$CLOCK/tabata"
    const val FOR_TIME = "$CLOCK/fortime"

    // More Options
    const val HISTORY = "history"

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
            HomeScreen(
                listState = listState,
                onHistoryClick = {
                    navController.navigate(HISTORY)
                }
            ) { type ->
                when(type) {
                    ClockType.AMRAP -> navController.navigate(AMRAP)
                    ClockType.EMOM -> navController.navigate(EMOM)
                    ClockType.TABATA -> navController.navigate(TABATA)
                    ClockType.FOR_TIME -> navController.navigate(FOR_TIME)
                }
            }
        }
        composable(AMRAP) {
            AmrapScreen {
                navController.popBackStack()
            }
        }
        composable(EMOM) {
            EmomScreen {
                navController.popBackStack()
            }
        }
        composable(TABATA) {
            TabataScreen {
                navController.popBackStack()
            }
        }
        composable(FOR_TIME) {
            ForTimeScreen {
                navController.popBackStack()
            }
        }
        composable(HISTORY) {
            History()
        }
    }
}

