package se.wmuth.openc25k.both

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION
import android.media.AudioAttributes.USAGE_ALARM
import android.media.MediaPlayer
import se.wmuth.openc25k.R

/**
 * Used to create the beeping noise in the application
 * Uses MediaPlayer to play the raw mp3-file in the project
 *
 * @param pCon the context of the parent of the beeper
 * @param vol the initial volume of the beeper, 0.0 to 1.0
 * @constructor Creates beeper with standard attributes
 */
class Beeper(pCon: Context, vol: Float) : MediaPlayer.OnCompletionListener {
    private val mp: MediaPlayer
    private var playCount: UInt = 0u

    init {
        val file: AssetFileDescriptor = pCon.resources.openRawResourceFd(R.raw.beep)
        mp = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes
                    .Builder()
                    .setContentType(CONTENT_TYPE_SONIFICATION)
                    .setUsage(USAGE_ALARM)
                    .build()
            )
            setVolume(vol, vol)
            setDataSource(file.fileDescriptor, file.startOffset, file.length)
        }
        mp.prepare()
        mp.setOnCompletionListener(this)
        file.close()
    }

    /**
     * Makes the Beeper beep
     */
    fun beep() {
        mp.start()
    }

    /**
     * Makes the Beeper beep a [number] of times
     */
    fun beepMultiple(number: UInt) {
        playCount = number - 1u
        beep()
    }

    override fun onCompletion(p0: MediaPlayer?) {
        if (playCount > 0u) {
            playCount--
            beep()
        }
    }
}