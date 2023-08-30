@file:Suppress("CyclicClassDependency")

package se.wmuth.openc25k.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import se.wmuth.openc25k.R
import se.wmuth.openc25k.data.Run

/**
 * Used to adapt an array of runs for display in a RecyclerView
 *
 * @param parentContext the context for the adapter
 * @param runsArr the array of runs to display
 * @param toListen a listener which listens to click events on the adapter and it's items
 * @constructor returns the adapter based on the input variables
 */
class RunAdapter(parentContext: Context, runsArr: Array<Run>, toListen: RunAdapterClickListener) :
    RecyclerView.Adapter<RunAdapter.RunViewHolder>() {
    private val context: Context = parentContext
    private val listener: RunAdapterClickListener = toListen
    private val runs: Array<Run> = runsArr

    interface RunAdapterClickListener {
        /**
         * Whenever a run in the adapter is clicked
         * this is called with the position in the adapter
         * a.k.a. index in array of the clicked item
         */
        fun onRunItemClick(position: Int)

        /**
         * Whenever a run in the adapter is long clicked (held)
         * this is called with the position in the adapter
         * a.k.a. index in array of the clicked item
         */
        fun onRunItemLongClick(position: Int)
    }

    /**
     * A class which holds each view for the adapter and detects clicks
     * @param itemView the view to hold
     * @constructor creates the standard implementation with onclick listeners for the itemView
     */
    inner class RunViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener {
        val iw: ImageView
        val tw: TextView

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
            iw = itemView.findViewById(R.id.imageView)
            tw = itemView.findViewById(R.id.textView)
        }

        override fun onClick(p0: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onRunItemClick(position)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onRunItemLongClick(position)
                return true
            }
            return false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(LayoutInflater.from(context).inflate(R.layout.run_row, parent, false))
    }

    override fun getItemCount(): Int {
        return runs.size
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val curRun = runs[position]
        holder.apply {
            tw.text = curRun.name

            val img = if (curRun.isComplete) {
                AppCompatResources.getDrawable(
                    context,
                    com.google.android.material.R.drawable.btn_checkbox_checked_mtrl
                )
            } else {
                AppCompatResources.getDrawable(
                    context,
                    com.google.android.material.R.drawable.btn_checkbox_unchecked_mtrl
                )
            }

            iw.setImageDrawable(img)
        }
    }
}