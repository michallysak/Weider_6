package pl.michallysak.weider6.training

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.michallysak.weider6.R
import pl.michallysak.weider6.database.TrainingDay
import pl.michallysak.weider6.database.TrainingDayDatabase
import pl.michallysak.weider6.logger
import pl.michallysak.weider6.ui.training.TrainingActivity
import java.util.*


class TrainingService : Service() {

    //beep
    private var soundPool: SoundPool? = null
    private var beep: Int? = null
    //tts
    private lateinit var textToSpeech: TextToSpeech
    private var lastTST = ""
    //tts number arrays
    private lateinit var numbers: Array<String>
    private lateinit var seriesNumbers: Array<String>
    private lateinit var exerciseNumbers: Array<String>
    //
    private val actionBase = "pl.michallysak.weider6.training"
    private var trainingIntent: Intent = Intent()
    private lateinit var trainingNotification: TrainingNotification
    private lateinit var database: TrainingDayDatabase
    private lateinit var lastTrainingDay: TrainingDay
    private var trainingManager: TrainingManager? = null
    private var isRunning = false
    //Service
    private var iBinder = TrainingBinder()

    inner class TrainingBinder : Binder() {

        fun getService(): TrainingService {
            return this@TrainingService
        }

    }


    //update ui
    private val trainingEvent = object: TrainingEvent{

        override fun onTrainingStart(scheduledSeries: Int, scheduledExercises: Int, scheduledRepeats: Int) {
            trainingIntent.putExtra("current_state", "START")
            trainingIntent.putExtra("scheduledSeries", scheduledSeries)
            trainingIntent.putExtra("scheduledExercises", scheduledExercises)
            trainingIntent.putExtra("scheduledRepeats", scheduledRepeats)
            sendIntentToBroadcastReceiver(trainingIntent)
        }

        override fun onBeforeTrainingCountdownUpdate(timeToTraining: Int) {
            trainingIntent.putExtra("current_state", "COUNTDOWN")
            trainingIntent.putExtra("timeToTraining", timeToTraining)
            trainingIntent.putExtra("commandText", "${getString(R.string.training_will_start_in)}: $timeToTraining")
            if (lastTST !=  "COUNTDOWN")
                speak(numbers[timeToTraining])
            else
                lastTST = "COUNTDOWN"
            sendIntentToBroadcastReceiver(trainingIntent)
            updateProgressOnNotification()
        }

        override fun onSeriesStart(seriesNumber: Int) {
            trainingIntent.putExtra("current_state", "SERIES")
            trainingIntent.putExtra("seriesNumber", seriesNumber)
            trainingIntent.putExtra("commandText", "")
            if (lastTST !=  "SERIES")
                speak(seriesNumbers[seriesNumber-1])
            else
                lastTST = "SERIES"
            sendIntentToBroadcastReceiver(trainingIntent)
            updateProgressOnNotification()
        }

        override fun onBetweenSeriesBreakUpdate(timeToNewSeries: Int) {
            trainingIntent.putExtra("current_state", "AFTER_SERIES_BREAK")
            trainingIntent.putExtra("timeToNewSeries", timeToNewSeries)
            trainingIntent.putExtra("commandText", "${getString(R.string.to_new_series)}: $timeToNewSeries")
            if (lastTST !=  "AFTER_SERIES_BREAK" && timeToNewSeries <= 10)
                speak(numbers[timeToNewSeries])
            else
                lastTST = "AFTER_SERIES_BREAK"
            sendIntentToBroadcastReceiver(trainingIntent)
            updateProgressOnNotification()
        }

        override fun onExerciseStart(exerciseNumber: Int) {
            trainingIntent.putExtra("current_state", "EXERCISE")
            trainingIntent.putExtra("exerciseNumber", exerciseNumber)
            trainingIntent.putExtra("commandText", "")
            if (lastTST !=  "EXERCISE")
                speak(exerciseNumbers[exerciseNumber-1])
            else
                lastTST = "EXERCISE"
            sendIntentToBroadcastReceiver(trainingIntent)
            updateProgressOnNotification()
        }

        override fun onBetweenExerciseBreakUpdate(timeToNewExercise: Int) {
            trainingIntent.putExtra("current_state", "AFTER_EXERCISES_BREAK")
            trainingIntent.putExtra("timeToNewExercise", timeToNewExercise)
            trainingIntent.putExtra("commandText", "${getString(R.string.to_new_exercise)}: $timeToNewExercise")
            if (lastTST !=  "AFTER_EXERCISES_BREAK" && timeToNewExercise <= 10)
                speak(numbers[timeToNewExercise])
            else
                lastTST = "AFTER_EXERCISES_BREAK"
            sendIntentToBroadcastReceiver(trainingIntent)
            updateProgressOnNotification()
        }

        override fun onRepeatStart(repeatNumber: Int) {
            trainingIntent.putExtra("current_state", "REPEAT")
            trainingIntent.putExtra("repeatNumber", repeatNumber)
            trainingIntent.putExtra("commandText", "")

            val tempText = if (trainingIntent.getIntExtra("exerciseNumber", 1) == 5 && repeatNumber != 1)
                getString(R.string.tst_switch)
            else
                getString(R.string.tst_up)

            if (lastTST !=  "COUNTDOWN")
                speak(tempText)
            else
                lastTST = "COUNTDOWN"

            sendIntentToBroadcastReceiver(trainingIntent)
            updateProgressOnNotification()
        }

        override fun onRepeatEnd(lastRepeat: Boolean) {
            trainingIntent.putExtra("current_state", "AFTER_REPEAT")

            var tempText = ""

            if (trainingIntent.getIntExtra("exerciseNumber", 1) != 5)
                tempText = getString(R.string.tst_down)
            else if(lastRepeat)
                tempText = getString(R.string.tst_down)

            if (lastTST !=  "AFTER_REPEAT")
                speak(tempText)
            else
                lastTST = "AFTER_REPEAT"

            sendIntentToBroadcastReceiver(trainingIntent)
        }

        override fun onRepeatCountdown(countDownTime: Int) {
            soundPool?.play(beep as Int, 1F, 1F, 0, 0, 1F)
        }

        override fun onTrainingEnd(passed: Boolean) {
            trainingIntent.putExtra("current_state", "FINISHED")
            trainingIntent.putExtra("passed", passed)
            trainingIntent.putExtra("commandText", getString(R.string.tst_finished))
            if (passed){
                speak(getString(R.string.tst_finished))

                updateProgressOnNotification()

                CoroutineScope(Dispatchers.IO).launch {
                    lastTrainingDay.passed = 1
                    database.trainingDao().update(lastTrainingDay)
                }

            }

            sendIntentToBroadcastReceiver(trainingIntent)


        }

    }


    //createService
    private fun createService(){
        numbers = resources.getStringArray(R.array.numbers)
        seriesNumbers = resources.getStringArray(R.array.series_numbers)
        exerciseNumbers = resources.getStringArray(R.array.exercise_numbers)

        trainingNotification = TrainingNotification(this)

        startForeground(1, trainingNotification.getNotification(isRunning, null, null))

        CoroutineScope(Dispatchers.IO).launch {
            database = Room.databaseBuilder(applicationContext, TrainingDayDatabase::class.java, "trainingsDatabase").build()
            lastTrainingDay = database.trainingDao().lastPassedTrainingDay
            val scheduledSeries = lastTrainingDay.series
            val scheduledRepeats = lastTrainingDay.repeat

            setupSounds()
            setupTTS()
            setupIntent(scheduledSeries, scheduledRepeats)

            trainingManager = TrainingManager(this@TrainingService, scheduledSeries, scheduledRepeats, trainingEvent)

            sendIntentToBroadcastReceiver(trainingIntent)

            updateProgressOnNotification()
        }

    }


    //fun called from TrainingActivity
    fun isRunning(): Boolean{
        return isRunning
    }

    fun onIsRunningChanges(){
        if (isRunning){
            trainingManager?.stop()
            soundPool?.autoPause()
        }else{
            trainingManager?.start()
        }

        isRunning = !isRunning

        trainingIntent.putExtra("isRunning", isRunning)
        sendIntentToBroadcastReceiver(trainingIntent)
        trainingNotification.notify(1, trainingNotification.getNotification(isRunning, null, null))

    }

    fun getIntent(): Intent{
        return trainingIntent
    }

    //update notification on progress
    private fun updateProgressOnNotification(){
        val seriesNumber = trainingIntent.getIntExtra("seriesNumber", 1)
        val exerciseNumber = trainingIntent.getIntExtra("exerciseNumber", 1)
        val repeatNumber = trainingIntent.getIntExtra("repeatNumber", 1)
        val scheduledSeries = trainingIntent.getIntExtra("scheduledSeries", 1)
        val scheduledRepeats = trainingIntent.getIntExtra("scheduledRepeats", 1)

        val title = "${getString(R.string.series)}: $seriesNumber/${scheduledSeries} " +
                "${getString(R.string.exercise)}: $exerciseNumber/6 " +
                "${getString(R.string.repeat)}: $repeatNumber/$scheduledRepeats"
        var text = trainingIntent.getStringExtra("commandText")

        if (trainingIntent.getStringExtra("current_state") == "REPEAT")
            text = getString(R.string.repeat)

        trainingNotification.notify(1, trainingNotification.getNotification(isRunning, title, text))
    }


    //send intent to TrainingActivity
    private fun sendIntentToBroadcastReceiver(intent: Intent){
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }


    //Service fun
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action){
            "$actionBase.CREATE_SERVICE" ->
                if (trainingManager == null) //check if training exist
                    createService()
                else
                    sendIntentToBroadcastReceiver(trainingIntent)

            "$actionBase.TRAINING_START_STOP" -> onIsRunningChanges()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return iBinder
    }

    override fun onDestroy() {
        trainingManager?.finish(false)
        releasePlayer()
        releaseTTS()
        stopSelf()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        trainingManager?.finish(false)
        stopSelf()
    }


    //intent
    private fun setupIntent(scheduledSeries: Int, scheduledRepeats: Int){
        trainingIntent = Intent(this@TrainingService, TrainingActivity::class.java)
        trainingIntent.action = actionBase

        trainingIntent.putExtra("current_state", "START")

        trainingIntent.putExtra("seriesNumber", 1)
        trainingIntent.putExtra("exerciseNumber", 1)
        trainingIntent.putExtra("repeatNumber", 1)

        trainingIntent.putExtra("scheduledSeries", scheduledSeries)
        trainingIntent.putExtra("scheduledExercises", 6)
        trainingIntent.putExtra("scheduledRepeats", scheduledRepeats)

        trainingIntent.putExtra("trainingDay", lastTrainingDay.day)
        trainingIntent.putExtra("trainingDay", lastTrainingDay.day)


        trainingIntent.putExtra("commandText", getString(R.string.click_button_to_start))
    }


    //beep
    @Suppress("DEPRECATION")
    private fun setupSounds() {

        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build()
        } else {
            SoundPool(1, AudioManager.STREAM_MUSIC, 0)
        }

        beep = soundPool?.load(this, R.raw.beep, 0)
    }

    private fun releasePlayer() {
        soundPool?.release()
        soundPool = null
    }


    //tts
    private fun setupTTS() {
        textToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS){
                val result = textToSpeech.setLanguage(Locale.getDefault())

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    val installIntent = Intent()
                    installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                    startActivity(installIntent)
                }
            }


        })
    }

    @Suppress("DEPRECATION")
    private fun speak(text: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }else{
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        }

    }

    private fun releaseTTS(){
        textToSpeech.stop()
        textToSpeech.shutdown()

    }

}