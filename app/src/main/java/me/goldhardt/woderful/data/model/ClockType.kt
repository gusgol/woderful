package me.goldhardt.woderful.data.model

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import me.goldhardt.woderful.R
import me.goldhardt.woderful.extensions.toMinutesAndSeconds
import kotlin.time.Duration.Companion.seconds

enum class ClockType(
    val displayName: Int,
    val image: Int,
    val roundBehavior: RoundBehavior
) : ClockMilestone, ClockSummaryDetails {

    AMRAP(R.string.amrap, R.drawable.img_wod_1, RoundBehavior.User) {
        override fun getThresholdS(configuration: WorkoutConfiguration): Long {
            return 60.seconds.inWholeSeconds
        }

        override fun getDetailsComposable(properties: Map<String, Any>): @Composable () -> Unit {
            return {
            }
        }
    },

    EMOM(R.string.emom, R.drawable.img_wod_2, RoundBehavior.Time) {
        override fun getThresholdS(configuration: WorkoutConfiguration): Long {
            return configuration.activeTimeS + configuration.restTimeS
        }

        override fun getDetailsComposable(properties: Map<String, Any>): @Composable () -> Unit {
            return {
                val activeTime = properties[ClockProperties.Configuration.CONFIG_ACTIVE_TIME_S] as? Double
                val roundCount = properties[ClockProperties.Configuration.CONFIG_ROUNDS] as? Double
                val restTime = properties[ClockProperties.Configuration.CONFIG_REST_TIME] as? Double

                if (activeTime != null && roundCount != null && restTime != null) {
                    val roundsDescription =
                        "${roundCount.toInt()} " +
                                pluralStringResource(
                                    id = R.plurals.message_rounds,
                                    count = roundCount.toInt()
                                )
                    val configurationSummary = stringResource(
                        R.string.title_emom_summary_desc,
                        activeTime.toInt().toMinutesAndSeconds(),
                        roundsDescription,
                        restTime.toInt().toMinutesAndSeconds()
                    )
                    Divider(
                        thickness = 1.dp,
                        color = MaterialTheme.colors.onSurface,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = configurationSummary,
                        style = MaterialTheme.typography.caption1,
                    )
                }
            }
        }
    },

    TABATA(R.string.tabata, R.drawable.img_wod_3, RoundBehavior.Time) {
        override fun getThresholdS(configuration: WorkoutConfiguration): Long {
            return configuration.activeTimeS + configuration.restTimeS
        }

        override fun getDetailsComposable(properties: Map<String, Any>): @Composable () -> Unit {
            return {
            }
        }
    },

    FOR_TIME(R.string.for_time, R.drawable.img_wod_4, RoundBehavior.User) {
        override fun getThresholdS(configuration: WorkoutConfiguration): Long {
            return 60.seconds.inWholeSeconds
        }

        override fun getDetailsComposable(properties: Map<String, Any>): @Composable () -> Unit {
            return {
            }
        }
    },
}

enum class RoundBehavior {
    Time, User
}