package se.wmuth.openc25k.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import se.wmuth.openc25k.databinding.VolumeDialogBinding

/**
 * Creates the volume selection dialog and updates the listener
 * when the user selects a new volume
 *
 * @param p the parent or listener to send the update volume event to
 * @param pCon the context of the parent
 * @param pLIf the layout inflater used in the parent so we can inflate the dialog
 */
class VolumeDialog(p: VolumeDialogListener, pCon: Context, pLIf: LayoutInflater) :
    OnSeekBarChangeListener, View.OnClickListener {
    private lateinit var dialog: AlertDialog
    private lateinit var twProgress: TextView
    private val parentContext: Context = pCon
    private val parentInflater: LayoutInflater = pLIf
    private val parent = p
    private var newVolume: Int = 0

    /**
     * Creates the volume alert dialog
     *
     * @param initialVol the initial volume to display, probably current selected volume
     */
    fun createAlertDialog(initialVol: Float) {
        newVolume = (initialVol * 100).toInt()

        val builder = AlertDialog.Builder(parentContext)
        val binding = VolumeDialogBinding.inflate(parentInflater)

        binding.seekbarVolume.progress = newVolume
        binding.seekbarVolume.setOnSeekBarChangeListener(this)

        binding.btnConfirm.setOnClickListener(this)

        twProgress = binding.twProgress
        twProgress.text = newVolume.toString()

        builder.setView(binding.root)
        dialog = builder.show()
    }

    interface VolumeDialogListener {
        /**
         * Set the volume in the listener to newVolume, [nV]
         */
        fun setVolume(nV: Float)
    }

    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
        if (p0 != null && p2) {
            newVolume = p1
            twProgress.text = p1.toString()
        }
    }

    override fun onClick(p0: View?) {
        parent.setVolume(newVolume.toFloat() / 100.0f)
        dialog.dismiss()
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {}
    override fun onStopTrackingTouch(p0: SeekBar?) {}
}