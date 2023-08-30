package se.wmuth.openc25k.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Stores the data for one interval e.g. title: "Walk", time: 300 (seconds)
 *
 * @param time the integer seconds the interval lasts for
 * @param title the title of what the user should do during, walk, run, warmup, etc
 * @constructor creates a single interval containing a title and time
 */
@Parcelize
data class Interval(val time: Int, val title: String) : Parcelable