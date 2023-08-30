package se.wmuth.openc25k.main

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import se.wmuth.openc25k.R

/**
 * Handles click events on the toolbar and calls the according
 * functions in the [SettingsMenuListener] interface
 *
 * @param p the parent, or listener for this class
 * @param s: the self, or menu that this class handles
 * @constructor Creates default object with passed values
 */
class SettingsMenu(p: SettingsMenuListener, s: Menu) : Toolbar.OnMenuItemClickListener {
    private val parent: SettingsMenuListener = p
    private val self: Menu = s

    interface SettingsMenuListener {
        /**
         * Creates the volume setting dialog
         */
        fun createVolumeDialog()

        /**
         * Returns the current sound setting
         * @return true if enabled, false if disabled
         */
        fun shouldMakeSound(): Boolean

        /**
         * Returns the current vibration setting
         * @return true if enabled, false if disabled
         */
        fun shouldVibrate(): Boolean

        /**
         * Plays a beep to allow the user to test their volume setting
         */
        fun testVolume()

        /**
         * Toggles the sound setting, true if was false and vice versa
         */
        fun toggleSound()

        /**
         * Toggles the vibration setting, true if was false and vice versa
         */
        fun toggleVibration()
    }

    override fun onMenuItemClick(p0: MenuItem?): Boolean {
        if (p0 == null) {
            return false
        }
        when (p0.itemId) {
            R.id.vibrate -> {
                parent.toggleVibration()
                self.findItem(R.id.vibrate).isChecked = parent.shouldVibrate()
            }

            R.id.sound -> {
                parent.toggleSound()
                self.findItem(R.id.sound).isChecked = parent.shouldMakeSound()
            }

            R.id.setVol -> parent.createVolumeDialog()
            R.id.testVol -> parent.testVolume()
            else -> return false
        }
        return true
    }
}