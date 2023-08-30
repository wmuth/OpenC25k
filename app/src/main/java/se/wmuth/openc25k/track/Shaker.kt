package se.wmuth.openc25k.track

import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.VibratorManager

/**
 * Handles vibration, or shaking, in the app
 *
 * @param manager the vibration manager to use when telling the device to vibrate
 * @constructor Creates default object and effects used later on
 */
class Shaker(manager: VibratorManager) {
    private val man: VibratorManager
    private val completeEffect: CombinedVibration
    private val walkEffect: CombinedVibration
    private val jogEffect: CombinedVibration
    private val delay: Long = 100L // default delay
    private val vib: Long = 350L // default time to vibrate

    init {
        man = manager

        val w = longArrayOf(0L, vib)
        walkEffect = CombinedVibration.createParallel(VibrationEffect.createWaveform(w, -1))

        val j = longArrayOf(0L, vib, delay, vib)
        jogEffect = CombinedVibration.createParallel(VibrationEffect.createWaveform(j, -1))

        val c = longArrayOf(0L, vib, delay, vib, delay, vib, delay, vib)
        completeEffect = CombinedVibration.createParallel(VibrationEffect.createWaveform(c, -1))
    }

    /**
     * Creates the shaking effect when switching to walking
     */
    fun walkShake() {
        shake(walkEffect)
    }

    /**
     * Creates the shaking effect when switching to jogging
     */
    fun jogShake() {
        shake(jogEffect)
    }

    /**
     * Creates the shaking effect when completing the run
     */
    fun completeShake() {
        shake(completeEffect)
    }

    private fun shake(e: CombinedVibration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            man.vibrate(e, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM))
        } else {
            man.vibrate(e)
        }
    }
}