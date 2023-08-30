package se.wmuth.openc25k.main

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.runBlocking
import se.wmuth.openc25k.R
import se.wmuth.openc25k.data.Interval
import se.wmuth.openc25k.data.Run

/**
 * Allows for easy editing of persistent data on the android device for the app
 *
 * Each data key we want to save has it's own getting and setting function
 * since this more easily allows me to be more typesafe, however there is
 * a decent amount of repeated code which could be excluded using e.g.
 * a enum class for which data you want and one getter function
 *
 * @param pCon the parent context
 * @param datastore the datastore to edit
 * @constructor Creates DataHandler of context which edits the datastore
 */
class DataHandler(pCon: Context, datastore: DataStore<Preferences>) {
    private val con: Context = pCon
    private val ds: DataStore<Preferences> = datastore

    /**
     * Gets the vibration enabled setting
     * @return the saved setting for vibration, default true
     */
    fun getVibrate(): Boolean {
        val v = booleanPreferencesKey("vibrate")
        var d: Boolean? = null
        runBlocking {
            ds.edit { settings ->
                d = settings[v]
            }
        }
        return d ?: true
    }

    /**
     * Gets the sound enabled setting
     * @return the saved setting for sound, default true
     */
    fun getSound(): Boolean {
        val s = booleanPreferencesKey("sound")
        var d: Boolean? = null
        runBlocking {
            ds.edit { settings ->
                d = settings[s]
            }
        }
        return d ?: true
    }

    /**
     * Gets the volume setting
     * @return the saved float value between 0 and 1.0, default 0.5
     */
    fun getVolume(): Float {
        val vol = floatPreferencesKey("volume")
        var d: Float? = null
        runBlocking {
            ds.edit { settings ->
                d = settings[vol]
            }
        }
        return d ?: 0.5f
    }

    /**
     * Gets the saved runs array
     * @return the saved runs with isComplete progress or default
     */
    fun getRuns(): Array<Run> {
        val r = stringPreferencesKey("runs")
        var d: String? = null
        runBlocking {
            ds.edit { settings ->
                d = settings[r]
            }
        }
        return deserialize(d) ?: defaultRuns()
    }

    /**
     * Persistently stores the vibration setting
     * @param vibrate whether vibration should be enabled or not
     */
    fun setVibrate(vibrate: Boolean) {
        val v = booleanPreferencesKey("vibrate")
        runBlocking {
            ds.edit { settings ->
                settings[v] = vibrate
            }
        }
    }

    /**
     * Persistently stores the sound setting
     * @param sound whether sound should be enabled or not
     */
    fun setSound(sound: Boolean) {
        val s = booleanPreferencesKey("sound")
        runBlocking {
            ds.edit { settings ->
                settings[s] = sound
            }
        }
    }

    /**
     * Persistently stores the volume setting
     * @param volume how loud the sound should be, 0.0 to 1.0
     */
    fun setVolume(volume: Float) {
        val vol = floatPreferencesKey("volume")
        runBlocking {
            ds.edit { settings ->
                settings[vol] = volume
            }
        }
    }

    /**
     * Persistently stores the runs array
     * @param runs the array of runs with new isComplete progress
     */
    fun setRuns(runs: Array<Run>) {
        val r = stringPreferencesKey("runs")
        runBlocking {
            ds.edit { settings ->
                settings[r] = serialize(runs)
            }
        }
    }

    /**
     * Extremely basic serialization function, could be improved
     * @param r Array of runs to serialize
     * @return the serialized string
     */
    private fun serialize(r: Array<Run>): String {
        var s = ""

        // For each run, destruct the run and add values with separator
        r.forEach { (name, description, isComplete, intervals) ->
            s += "$name|"
            s += "$description|"
            s += "$isComplete|"
            // Since intervals differs in length, for each append these too
            intervals.forEach { (time, title) ->
                s += "$time|"
                s += "$title|"
            }
            // Make sure each run ends with || for deserialization
            s += "|"
        }

        // Drop ending || so .split() doesn't result in empty index at the end
        s = s.dropLast(2)

        return s
    }


    /**
     * Extremely basic deserialization function, could be improved
     * @param d the data to deserialize
     * @return the deserialized array or null if invalid data
     */
    private fun deserialize(d: String?): Array<Run>? {
        if (d == null) {
            return null
        }

        // Create empty list and split string on || which separates each run
        val runs: MutableList<Run> = mutableListOf()
        val split = d.split("||")
        // For each run from the split
        split.forEach { severalRuns ->
            // Create an iterator which yields each field in the run
            val run = severalRuns.split("|").iterator()
            // First yields name
            val name = run.next()
            // Then description
            val desc = run.next()
            val isComplete = run.next().toBoolean()
            val intervals: MutableList<Interval> = mutableListOf()
            // Since intervals is a list we do a for each on the remaining items in the iterator
            run.forEach {
                intervals.add(Interval(it.toInt(), run.next()))
            }
            // Add all the values extracted
            runs.add(Run(name, desc, isComplete, intervals.toTypedArray()))
        }
        return runs.toTypedArray()
    }

    /**
     * Returns the default runs, hardcoded since it follows a real world regimen.
     * This whole structure could be massively improved, I have just done
     * the most basic implementation for this simple app.
     * Should be fast enough for the purposes
     * @return the default state of the runs array
     */
    private fun defaultRuns(): Array<Run> {
        val r = con.resources
        return arrayOf(
            Run(
                name = String.format(
                    "%s 1 %s 1", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w1d1),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                )
            ),
            Run(
                name = String.format(
                    "%s 1 %s 2", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w1d2),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                )
            ),
            Run(
                name = String.format(
                    "%s 1 %s 3", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w1d3),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(60, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                )
            ),

            Run(
                name = String.format(
                    "%s 2 %s 1", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w2d1),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(120, r.getString(R.string.walk)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(120, r.getString(R.string.walk)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(120, r.getString(R.string.walk)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(120, r.getString(R.string.walk)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(120, r.getString(R.string.walk)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(60, r.getString(R.string.walk)),
                )
            ),
            Run(
                name = String.format(
                    "%s 2 %s 2", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w2d2),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(120, r.getString(R.string.walk)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(120, r.getString(R.string.walk)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(120, r.getString(R.string.walk)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(120, r.getString(R.string.walk)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(120, r.getString(R.string.walk)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(60, r.getString(R.string.walk)),
                )
            ),
            Run(
                name = String.format(
                    "%s 2 %s 3", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w2d3),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(120, r.getString(R.string.walk)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(120, r.getString(R.string.walk)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(120, r.getString(R.string.walk)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(120, r.getString(R.string.walk)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(120, r.getString(R.string.walk)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(60, r.getString(R.string.walk)),
                )
            ),

            Run(
                name = String.format(
                    "%s 3 %s 1", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w3d1),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(180, r.getString(R.string.jog)),
                    Interval(180, r.getString(R.string.walk)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(180, r.getString(R.string.jog)),
                    Interval(180, r.getString(R.string.walk)),
                )
            ),
            Run(
                name = String.format(
                    "%s 3 %s 2", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w3d2),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(180, r.getString(R.string.jog)),
                    Interval(180, r.getString(R.string.walk)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(180, r.getString(R.string.jog)),
                    Interval(180, r.getString(R.string.walk)),
                )
            ),
            Run(
                name = String.format(
                    "%s 3 %s 3", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w3d3),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(180, r.getString(R.string.jog)),
                    Interval(180, r.getString(R.string.walk)),
                    Interval(90, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(180, r.getString(R.string.jog)),
                    Interval(180, r.getString(R.string.walk)),
                )
            ),

            Run(
                name = String.format(
                    "%s 4 %s 1", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w4d1),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(180, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(300, r.getString(R.string.jog)),
                    Interval(150, r.getString(R.string.walk)),
                    Interval(180, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(300, r.getString(R.string.jog)),
                )
            ),
            Run(
                name = String.format(
                    "%s 4 %s 2", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w4d2),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(180, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(300, r.getString(R.string.jog)),
                    Interval(150, r.getString(R.string.walk)),
                    Interval(180, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(300, r.getString(R.string.jog)),
                )
            ),
            Run(
                name = String.format(
                    "%s 4 %s 3", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w4d3),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(180, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(300, r.getString(R.string.jog)),
                    Interval(150, r.getString(R.string.walk)),
                    Interval(180, r.getString(R.string.jog)),
                    Interval(90, r.getString(R.string.walk)),
                    Interval(300, r.getString(R.string.jog)),
                )
            ),

            Run(
                name = String.format(
                    "%s 5 %s 1", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w5d1),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(300, r.getString(R.string.jog)),
                    Interval(180, r.getString(R.string.walk)),
                    Interval(300, r.getString(R.string.jog)),
                    Interval(180, r.getString(R.string.walk)),
                    Interval(300, r.getString(R.string.jog)),
                )
            ),
            Run(
                name = String.format(
                    "%s 5 %s 2", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w5d2),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(480, r.getString(R.string.jog)),
                    Interval(300, r.getString(R.string.walk)),
                    Interval(480, r.getString(R.string.jog)),
                )
            ),
            Run(
                name = String.format(
                    "%s 5 %s 3", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w5d3),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(1200, r.getString(R.string.jog)),
                )
            ),

            Run(
                name = String.format(
                    "%s 6 %s 1", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w6d1),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(300, r.getString(R.string.jog)),
                    Interval(180, r.getString(R.string.walk)),
                    Interval(480, r.getString(R.string.jog)),
                    Interval(180, r.getString(R.string.walk)),
                    Interval(300, r.getString(R.string.jog)),
                )
            ),
            Run(
                name = String.format(
                    "%s 6 %s 2", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w6d2),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(600, r.getString(R.string.jog)),
                    Interval(180, r.getString(R.string.walk)),
                    Interval(600, r.getString(R.string.jog)),
                )
            ),
            Run(
                name = String.format(
                    "%s 6 %s 3", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w6d3),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(1320, r.getString(R.string.jog)),
                )
            ),

            Run(
                name = String.format(
                    "%s 7 %s 1", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w7d1),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(1500, r.getString(R.string.jog)),
                )
            ),
            Run(
                name = String.format(
                    "%s 7 %s 2", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w7d2),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(1500, r.getString(R.string.jog)),
                )
            ),
            Run(
                name = String.format(
                    "%s 7 %s 3", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w7d3),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(1500, r.getString(R.string.jog)),
                )
            ),

            Run(
                name = String.format(
                    "%s 8 %s 1", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w8d1),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(1680, r.getString(R.string.jog)),
                )
            ),
            Run(
                name = String.format(
                    "%s 8 %s 2", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w8d2),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(1680, r.getString(R.string.jog)),
                )
            ),
            Run(
                name = String.format(
                    "%s 8 %s 3", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w8d3),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(1680, r.getString(R.string.jog)),
                )
            ),

            Run(
                name = String.format(
                    "%s 9 %s 1", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w9d1),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(1800, r.getString(R.string.jog)),
                )
            ),
            Run(
                name = String.format(
                    "%s 9 %s 2", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w9d2),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(1800, r.getString(R.string.jog)),
                )
            ),
            Run(
                name = String.format(
                    "%s 9 %s 3", r.getString(R.string.week), r.getString(R.string.day)
                ),
                description = r.getString(R.string.w9d3),
                isComplete = false,
                intervals = arrayOf(
                    Interval(300, r.getString(R.string.warmup)),
                    Interval(1800, r.getString(R.string.jog)),
                )
            ),
        )
    }
}