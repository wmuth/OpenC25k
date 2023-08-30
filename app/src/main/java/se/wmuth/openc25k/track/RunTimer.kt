package se.wmuth.openc25k.track

import android.os.CountDownTimer
import se.wmuth.openc25k.data.Interval

/**
 * Times an array of intervals, sending events throughout
 *
 * @param iVal the interval array to time
 * @param listener the listener which gets notified of the timer's events
 * @constructor Creates default implementation of timer to track the intervals
 */
class RunTimer(iVal: Array<Interval>, listener: RunTimerListener) {
    private val intervals: Iterator<Interval> = iVal.iterator()
    private val parent: RunTimerListener = listener
    private val totSeconds: Int = iVal.fold(0) { acc, (time) -> acc + time }
    private var curInterval: Interval = intervals.next()
    private var intervalSeconds: Int = 0
    private var timer: CountDownTimer? = null
    private var secondsPassed: Int = 0
    private var finished: Boolean = false
    private var thereIsTimer: Boolean = false

    interface RunTimerListener {
        /**
         * When the timer runs out of intervals to track this is called
         */
        fun finishRun()

        /**
         * When the current interval finishes and we move onto the next one,
         * this method is called
         */
        fun nextInterval()

        /**
         * On each tick, for now always 1 second long, this method is called
         */
        fun tick()
    }

    /**
     * Start the timer, period 1 second
     * Will resumed if paused earlier
     */
    fun start() {
        if (!finished && !thereIsTimer) {
            timer = object :
                CountDownTimer((((curInterval.time - intervalSeconds) * 1000).toLong()), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    tick()
                }

                override fun onFinish() {}
            }.start()

            thereIsTimer = true
        }
    }

    /**
     * Pauses the timer
     */
    fun pause() {
        timer?.cancel()
        thereIsTimer = false
    }

    /**
     * Skips the rest of the current interval and moves to the next one
     */
    fun skip() {
        pause()
        secondsPassed += (curInterval.time - intervalSeconds)
        intervalSeconds = 0
        if (intervals.hasNext()) {
            curInterval = intervals.next()
            parent.nextInterval()
            start()
        } else if (!finished) {
            parent.finishRun()
            finished = true
        }
    }

    /**
     * Gets the remaining time in the current interval
     * Format is MM:SS
     */
    fun getIntervalRemaining(): String {
        return String.format(
            "%02d:%02d",
            ((curInterval.time - intervalSeconds) / 60),
            ((curInterval.time - intervalSeconds) % 60)
        )
    }

    /**
     * Gets the remaining time in total for the entire run
     * Format is MM:SS
     */
    fun getTotalRemaining(): String {
        return String.format(
            "%02d:%02d", ((totSeconds - secondsPassed) / 60), ((totSeconds - secondsPassed) % 60)
        )
    }

    private fun tick() {
        intervalSeconds++
        secondsPassed++

        if (intervalSeconds >= curInterval.time) {
            skip()
        } else {
            parent.tick()
        }
    }
}