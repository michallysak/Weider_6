package pl.michallysak.weider6.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_how_to.view.*
import pl.michallysak.weider6.R



class HowToFragment : Fragment() {

    private val exerciseImages = listOf(
        R.drawable.exercise_1,
        R.drawable.exercise_2,
        R.drawable.exercise_3,
        R.drawable.exercise_4,
        R.drawable.exercise_5,
        R.drawable.exercise_6
    )

    inner class SampleExercise(val title: String, val description: String, val imageId: Int)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_how_to, container, false)

        val recyclerView: RecyclerView
        recyclerView = view.how_to_recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = HowToAdapter(generateSampleExercise(), context!!)

        return view
    }

    private fun generateSampleExercise(): List<SampleExercise>{
        val list = mutableListOf<SampleExercise>()

        val title = resources.getStringArray(R.array.exercise_titles)
        val descriptions = resources.getStringArray(R.array.exercise_descriptions)

        for (i in 0 until 6)
            list.add(SampleExercise(title[i], descriptions[i], exerciseImages[i]))

        return list
    }
}
