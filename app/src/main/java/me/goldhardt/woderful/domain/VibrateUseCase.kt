package me.goldhardt.woderful.domain

import android.os.VibrationEffect
import android.os.Vibrator

/**
 * Helper class to vibrate the device.
 */
class VibrateUseCase(
    private val vibrator: Vibrator,
) {

    /**
     * Vibrate for a given [duration] in milliseconds.
     */
    operator fun invoke(duration: Long) {
        vibrator.let { vibrator ->
            val vibrationEffect: VibrationEffect =
                VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.cancel()
            vibrator.vibrate(vibrationEffect)
        }
    }
}