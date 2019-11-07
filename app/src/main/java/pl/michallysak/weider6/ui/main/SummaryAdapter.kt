package pl.michallysak.weider6.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import pl.michallysak.weider6.R
import pl.michallysak.weider6.database.TrainingDay


class SummaryAdapter(private val trainingDays: List<TrainingDay>, private val context: Context) : RecyclerView.Adapter<SummaryAdapter.ViewHolder>() {

    inner class ViewHolder(pItem: View) : RecyclerView.ViewHolder(pItem) {
        var day: TextView = pItem.findViewById(R.id.summary_training_day)
        var series: TextView = pItem.findViewById(R.id.summary_training_series)
        var repeat: TextView = pItem.findViewById(R.id.summary_training_repeat)
        var passedImg: ImageView = pItem.findViewById(R.id.summary_training_passed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_training_day, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.day.text = trainingDays[position].day.toString()
        holder.series.text = trainingDays[position].series.toString()
        holder.repeat.text = trainingDays[position].repeat.toString()

        if (trainingDays[position].passed == 1)
            holder.passedImg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_check_circle))
    }

    override fun getItemCount() = trainingDays.size
}