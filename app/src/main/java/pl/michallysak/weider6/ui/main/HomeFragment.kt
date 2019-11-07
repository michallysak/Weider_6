package pl.michallysak.weider6.ui.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.room.Room
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.michallysak.weider6.R
import pl.michallysak.weider6.database.TrainingDay
import pl.michallysak.weider6.database.TrainingDayDatabase
import pl.michallysak.weider6.database.generateTraining
import pl.michallysak.weider6.ui.training.TrainingActivity


class HomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        view.go_button.setOnClickListener { startActivity(Intent(context, TrainingActivity::class.java))}
        view.current_training_day_and_details_box.setOnClickListener { Navigation.findNavController(view).navigate(R.id.destination_summary)}

        CoroutineScope(Dispatchers.IO).launch {
            val database = Room.databaseBuilder(context as Context, TrainingDayDatabase::class.java, "trainingsDatabase").build()
            val training = database.trainingDao().lastPassedTrainingDay
            setTextView(view, "${getString(R.string.day)} ${training.day}", "${training.repeat} x ${training.series}")
        }

        return view
    }

    private fun setTextView(view: View, dayText: String, dayDetails: String){
        CoroutineScope(Dispatchers.Main).launch {
            view.current_training_day.text = dayText
            view.current_training_details.text = dayDetails
        }
    }

    override fun onResume() {
        CoroutineScope(Dispatchers.IO).launch {
            val trainingDays: List<TrainingDay>
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val database = Room.databaseBuilder(context as Context, TrainingDayDatabase::class.java, "trainingsDatabase").build()
            if (!preferences.getBoolean("training_active", false)){
                trainingDays = generateTraining(42)
                database.trainingDao().insertAll(trainingDays)
                preferences.edit().putBoolean("training_active", true).apply()
            }else{
                trainingDays = database.trainingDao().all
                if (preferences.getBoolean("training_completed_dialog", false)){
                    preferences.edit().putBoolean("training_completed_dialog", true).apply()
                    AlertDialog.Builder(context as Context)
                        .setCancelable(false)
                        .setTitle(R.string.congratulations)
                        .setMessage(R.string.you_have_completed_all_training_days)
                        .setPositiveButton(R.string.ok) { dialog, _ ->  dialog.dismiss()}
                        .create().show()
                }
            }
            (context as MainActivity).trainingDays = trainingDays
        }
        super.onResume()
    }


}
