package se.wmuth.openc25k

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.recyclerview.widget.LinearLayoutManager
import se.wmuth.openc25k.both.Beeper
import se.wmuth.openc25k.data.Run
import se.wmuth.openc25k.databinding.ActivityMainBinding
import se.wmuth.openc25k.main.DataHandler
import se.wmuth.openc25k.main.RunAdapter
import se.wmuth.openc25k.main.SettingsMenu
import se.wmuth.openc25k.main.VolumeDialog

// Get the datastore for the app
val Context.datastore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * The main activity, ties together everything on the apps 'home' page
 */
class MainActivity : AppCompatActivity(), RunAdapter.RunAdapterClickListener,
    SettingsMenu.SettingsMenuListener, VolumeDialog.VolumeDialogListener {
    private lateinit var menu: SettingsMenu
    private lateinit var runs: Array<Run>
    private lateinit var volDialog: VolumeDialog
    private lateinit var handler: DataHandler
    private lateinit var adapter: RunAdapter
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private var sound: Boolean = true
    private var vibrate: Boolean = true
    private var volume: Float = 0.5f

    override fun onCreate(savedInstanceState: Bundle?) {
        handler = DataHandler(this, datastore)
        sound = handler.getSound()
        vibrate = handler.getVibrate()
        volume = handler.getVolume()
        runs = handler.getRuns()

        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        volDialog = VolumeDialog(this, this, layoutInflater)
        menu = SettingsMenu(this, binding.materialToolbar.menu)

        val runRV = binding.recyclerView
        adapter = RunAdapter(this, runs, this)
        runRV.adapter = adapter
        runRV.layoutManager = LinearLayoutManager(this)

        binding.materialToolbar.setOnMenuItemClickListener(menu)
        binding.materialToolbar.menu.findItem(R.id.vibrate).isChecked = vibrate
        binding.materialToolbar.menu.findItem(R.id.sound).isChecked = sound

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            handleActivityResult(it.resultCode, it.data)
        }
    }

    override fun onRunItemClick(position: Int) {
        // Run was clicked, launch TrackActivity with extras
        val intent = Intent(this, TrackActivity::class.java)
        intent.putExtra("run", runs[position])
        intent.putExtra("id", position)
        intent.putExtra("sound", sound)
        intent.putExtra("vibrate", vibrate)
        intent.putExtra("volume", volume)
        launcher.launch(intent)
    }

    override fun onRunItemLongClick(position: Int) {
        // Run held, toggle isComplete
        runs[position].isComplete = !runs[position].isComplete
        adapter.notifyItemChanged(position)
        handler.setRuns(runs)
    }

    /**
     * Handle the result of the TrackActivity
     *
     * @param resultCode if RESULT_OK, run was finished
     * @param data the data sent back from the activity, which run was completed
     */
    private fun handleActivityResult(resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && data != null) {
            runs[data.getIntExtra("id", 0)].isComplete = true
            adapter.notifyItemChanged(data.getIntExtra("id", 0))
            handler.setRuns(runs)
        }
    }

    override fun createVolumeDialog() {
        volDialog.createAlertDialog(volume)
    }

    override fun shouldMakeSound(): Boolean {
        return sound
    }

    override fun shouldVibrate(): Boolean {
        return vibrate
    }

    override fun testVolume() {
        val beeper = Beeper(applicationContext, volume)
        if (sound) {
            beeper.beep()
        }
    }

    override fun toggleSound() {
        sound = !sound
        handler.setSound(sound)
    }

    override fun toggleVibration() {
        vibrate = !vibrate
        handler.setVibrate(vibrate)
    }

    override fun setVolume(nV: Float) {
        volume = nV
        handler.setVolume(volume)
    }
}