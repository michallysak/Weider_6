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


class HowToAdapter(private val trainings: List<HowToFragment.SampleExercise>, private val context: Context) : RecyclerView.Adapter<HowToAdapter.ViewHolder>() {

    inner class ViewHolder(pItem: View) : RecyclerView.ViewHolder(pItem) {
        var title: TextView = pItem.findViewById(R.id.exercise_card_title)
        var description: TextView = pItem.findViewById(R.id.exercise_card_description)
        var image: ImageView = pItem.findViewById(R.id.exercise_card_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_exercise_card, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = trainings[position].title
        holder.description.text = trainings[position].description
        holder.image.setImageDrawable(ContextCompat.getDrawable(context, trainings[position].imageId))
    }

    override fun getItemCount() = trainings.size
}