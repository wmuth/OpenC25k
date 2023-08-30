package se.wmuth.openc25k

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibratorManager
import androidx.appcompat.app.AppCompatActivity
import se.wmuth.openc25k.both.Beeper
import se.wmuth.openc25k.data.Interval
import se.wmuth.openc25k.data.Run
import se.wmuth.openc25k.databinding.ActivityTrackBinding
import se.wmuth.openc25k.track.RunTimer
import se.wmuth.openc25k.track.Shaker

/**
 * Combines all the tracking parts of the app to one cohesive whole
 * Should be sent intent with keys
 *      "id" -> index of the run in the recyclerview: Int
 *      "run" -> run object to track: Run
 *      "sound" -> if sound is enabled: Boolean
 *      "vibrate" -> if vibration is enabled: Boolean
 *      "volume" -> what the volume is: Float
 */
class TrackActivity : AppCompatActivity(), RunTimer.RunTimerListener {
    private lateinit var beeper: Beeper
    private lateinit var binding: ActivityTrackBinding
    private lateinit var intentReturn: Intent
    private lateinit var intervals: Iterator<Interval>
    private lateinit var shaker: Shaker
    private lateinit var timer: RunTimer
    private var sound: Boolean = true
    private var vibrate: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val run = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("run", Run::class.java) ?: return
        } else {
            // Above is more typesafe but only works on newer SDK
            @Suppress("DEPRECATION")
            (intent.getParcelableExtra("run") ?: return)
        }

        intervals = run.intervals.iterator()
        sound = intent.getBooleanExtra("sound", true)
        vibrate = intent.getBooleanExtra("vibrate", true)

        beeper = Beeper(this, intent.getFloatExtra("volume", 0.5f))
        shaker = Shaker(getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager)
        timer = RunTimer(run.intervals, this)

        binding.twRemainTimer.text = timer.getTotalRemaining()
        binding.twStatus.text = intervals.next().title
        binding.twSummary.text = run.description
        binding.twTimer.text = timer.getIntervalRemaining()
        binding.twTitle.text = run.name

        binding.btnPause.setOnClickListener { timer.pause() }
        binding.btnSkip.setOnClickListener { timer.skip() }
        binding.btnStart.setOnClickListener { timer.start() }

        intentReturn = Intent()
        intentReturn.putExtra("id", intent.getIntExtra("id", 0))
        setResult(RESULT_CANCELED, intentReturn)
    }

    override fun tick() {
        runOnUiThread {
            binding.twRemainTimer.text = timer.getTotalRemaining()
            binding.twTimer.text = timer.getIntervalRemaining()
        }
    }

    override fun nextInterval() {
        val next = intervals.next()
        runOnUiThread {
            binding.twStatus.text = next.title
            binding.twTimer.text = next.time.toString()
        }

        if (next.title == getString(R.string.walk)) {
            if (sound) {
                beeper.beep()
            }
            if (vibrate) {
                shaker.walkShake()
            }
        } else {
            if (sound) {
                beeper.beepMultiple(2u)
            }
            if (vibrate) {
                shaker.jogShake()
            }
        }
    }

    override fun finishRun() {
        runOnUiThread {
            binding.twStatus.text = getString(R.string.runComplete)
        }
        setResult(RESULT_OK, intentReturn)
        if (sound) {
            beeper.beepMultiple(4u)
        }
        if (vibrate) {
            shaker.completeShake()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.pause()
    }
}