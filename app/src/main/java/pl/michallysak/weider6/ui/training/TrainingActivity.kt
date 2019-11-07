package pl.michallysak.weider6.ui.training

import android.content.*
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.preference.PreferenceManager
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_training.*
import pl.michallysak.weider6.R
import pl.michallysak.weider6.isDarkTheme
import pl.michallysak.weider6.logger
import pl.michallysak.weider6.training.TrainingService

class TrainingActivity : AppCompatActivity() {

    //view
    private lateinit var screenRotationSwitcher: ImageButton
    private lateinit var dayTextView: TextView
    private lateinit var commandTextView: TextView
    private lateinit var seriesText: TextView
    private lateinit var exerciseText: TextView
    private lateinit var repeatText: TextView
    private lateinit var exerciseImage: ImageView
    private lateinit var fab: FloatingActionButton

    private val exerciseImages = listOf(
        R.drawable.exercise_0,
        R.drawable.exercise_1,
        R.drawable.exercise_2,
        R.drawable.exercise_3,
        R.drawable.exercise_4,
        R.drawable.exercise_5,
        R.drawable.exercise_6
    )
    private val actionBase = "pl.michallysak.weider6.training"

    private var bound = false
    private var isDark = false
    private lateinit var preferences: SharedPreferences
    private lateinit var trainingService: TrainingService

    private var intentFromService: Intent? = null


    private var serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TrainingService.TrainingBinder
            trainingService = binder.getService()
//            restoreTrainingData
            intentFromService = trainingService.getIntent()
            intentFromService?.let { updateAllUI(it) }
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
        }

    }


    //setup and update view
    private fun setupUI() {
        supportActionBar?.hide()
        screenRotationSwitcher = screen_rotation_switcher
        commandTextView = command_text
        fab = fab_play_stop
        seriesText = series
        exerciseText = exercise
        repeatText = repeat
        exerciseImage = exercise_image
        dayTextView = day

        fab.setOnClickListener {
            trainingService.onIsRunningChanges()
        }
    }

    private fun allowRotationOnLandscape(fullRotation: Boolean) {
        screenRotationSwitcher.visibility = View.INVISIBLE

        requestedOrientation = if (fullRotation)
            ActivityInfo.SCREEN_ORIENTATION_USER
        else
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        Handler().postDelayed({

            screenRotationSwitcher.visibility = View.VISIBLE

            when (windowManager.defaultDisplay.rotation) {
                0 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                1 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                3 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            }


        }, 5000)
    }

    private fun setupOrientation() {
        if (preferences.getString("screen_orientation", "auto") == "portrait") {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            screenRotationSwitcher.visibility = View.GONE
        } else {
            val fullRotation = preferences.getString("screen_orientation", "auto") == "auto"

            allowRotationOnLandscape(fullRotation)
            screenRotationSwitcher.setOnClickListener { allowRotationOnLandscape(fullRotation) }
        }

        if (preferences.getBoolean("keep_screen_on", true))
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    }

    private fun updateDay(dayNumber: Int){
        val text = "${getString(R.string.day)} $dayNumber"
        dayTextView.text = text
    }

    private fun updateImage(exercise: Int){
        if (exercise in 0..6) {
            exerciseImage.visibility = View.VISIBLE
            exerciseImage.setImageDrawable(ContextCompat.getDrawable(this, exerciseImages[exercise]))
        }else{
            exerciseImage.visibility = View.GONE
        }
    }

    private fun updateSeries(series: Int, scheduledSeries: Int) {
        val text = "$series/$scheduledSeries"
        seriesText.text = text
    }

    private fun updateExercise(exercise: Int, scheduledExercise: Int) {
        val text = "$exercise/$scheduledExercise"
        exerciseText.text =  text
    }

    private fun updateRepeat(repeat: Int, scheduledRepeat: Int) {
        val text = "$repeat/$scheduledRepeat"
        repeatText.text = text
    }

    private fun updateCommandText(text: String?) {
        if (text != null){
            commandTextView.visibility = View.VISIBLE
            commandTextView.text = text
        }else if (text == ""){
            commandTextView.visibility = View.GONE
        }
    }

    private fun updateAllUI(intent: Intent){
        logger("UI ${intent.getStringExtra("current_state")}")
        updateSeries(intent.getIntExtra("seriesNumber", 1), intent.getIntExtra("scheduledSeries", 1))
        updateExercise(intent.getIntExtra("exerciseNumber", 1), intent.getIntExtra("scheduledExercises", 1))
        updateRepeat(intent.getIntExtra("repeatNumber", 1), intent.getIntExtra("scheduledRepeats", 1))
        updateCommandText(intent.getStringExtra("commandText"))
        updateDay(intent.getIntExtra("", 1))
        updateFabDrawable(intent.getBooleanExtra("isRunning", false))
        updateImage(intent.getIntExtra("exerciseNumber", -1))
    }

    private fun updateFabDrawable(isRunning: Boolean){
        when (isRunning) {
            true -> fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause))
            false -> fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play))
        }

    }


    //will update the interface when the service is bounded
    private fun onBroadcastReceive(intent: Intent) {
        when (intent.getStringExtra("current_state")) {
            "START" ->{
                updateAllUI(intent)
                updateImage(-1)
            }
//            "COUNTDOWN"
            "SERIES" -> {
                val seriesNumber = intent.getIntExtra("seriesNumber", 1)
                updateSeries(seriesNumber, intent.getIntExtra("scheduledSeries", 1))
            }

            "EXERCISE" -> {
                val exerciseNumber = intent.getIntExtra("exerciseNumber", 1)
                updateExercise(exerciseNumber, intent.getIntExtra("scheduledExercises", 1))
                updateImage(0)
            }

            "REPEAT" -> {
                val exerciseNumber = intent.getIntExtra("exerciseNumber", 1)
                val repeatNumber = intent.getIntExtra("repeatNumber", 1)
                updateRepeat(repeatNumber, intent.getIntExtra("scheduledRepeats", 1))
                updateImage(exerciseNumber)
            }

            "AFTER_REPEAT" -> {
                if(intent.getIntExtra("exerciseNumber", 1) != 5)
                    updateImage(0)
            }

            "AFTER_EXERCISES_BREAK",
            "AFTER_SERIES_BREAK",
            "FINISHED"-> {
                updateImage(-1)
            }
        }


        updateFabDrawable(intent.getBooleanExtra("isRunning", false))

        updateCommandText(intent.getStringExtra("commandText"))
    }


    //lifecycle
    override fun onStart() {
        super.onStart()
        val intent = Intent(this, TrainingService::class.java)
        intent.action = "$actionBase.CREATE_SERVICE"
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)

        isDark = isDarkTheme(this)

        setupUI()

        intentFromService = intent

        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        setupOrientation()

        LocalBroadcastManager.getInstance(this).registerReceiver(
            object: BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (bound) {
                        onBroadcastReceive(intent as Intent)
                        intentFromService = intent
                    }
                }
            }, IntentFilter(actionBase)
        )


    }

    override fun onStop() {
        super.onStop()
        try {
            unbindService(serviceConnection)
            bound = false
        } catch (e: Exception) {
            logger(e.message as String)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(this, TrainingService::class.java)
        if (PreferenceManager.getDefaultSharedPreferences(this).getString("theme", "default") != "default"
            && isDarkTheme(this) != isDark){
            stopService(intent)
        }

    }

    override fun finish() {
        val intent = Intent(this, TrainingService::class.java)
        stopService(intent)
        super.finish()
    }

    override fun onBackPressed() {

        if (bound && trainingService.isRunning()){
            trainingService.onIsRunningChanges()
        }

        AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle(R.string.exit_training)
            .setPositiveButton(R.string.yes) { _, _ ->
                finish()
            }
            .setNegativeButton(R.string.no) { _, _ -> }
            .show()
    }


}
