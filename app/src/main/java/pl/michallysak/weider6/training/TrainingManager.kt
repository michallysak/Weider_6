package pl.michallysak.weider6.training

import androidx.preference.PreferenceManager
import kotlinx.coroutines.*
import pl.michallysak.weider6.logger


class TrainingManager(
    val context: TrainingService,
    private val scheduledSeries: Int, private val scheduledRepeats: Int,
    private val trainingEvent: TrainingEvent?) : TrainingControl {

    private var scheduledExercises = 6
    private var currentSeries = 1
    private var currentExercise = 1
    private var currentRepeat = 1
    private var breakBetweenSeries = 10
    private var breakBetweenExercises = 5
    private var pendingTime = 0
    private var currentState: TrainingState = TrainingState.COUNTDOWN

    private var trainingJob: Job? = null
    private val countdownDuration = 5
    private val repeatDuration = 3
    private var passed = false

    init {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        breakBetweenSeries = preferences.getString("break_between_series", "10")!!.toInt()
        breakBetweenExercises = preferences.getString("break_between_exercises", "5")!!.toInt()
        trainingEvent?.onTrainingStart(scheduledSeries, scheduledExercises, scheduledRepeats)
    }

    //tools
    private fun log(text: String) {
        logger("TrainingManager $currentSeries $currentExercise $currentRepeat $text $currentState")
    }

    private fun setState(state: TrainingState) = apply { this.currentState = state }

    //TrainingControl
    override fun start() {
        start(currentState)
    }

    override fun stop() {
        trainingJob?.cancel()
    }

    override fun finish(passed: Boolean) {
        trainingJob?.cancel()
        trainingEvent?.onTrainingEnd(passed)
    }

    override fun start(state: TrainingState) {

        log("start $currentState")
        setState(state)

        trainingJob = when (currentState) {
            TrainingState.COUNTDOWN -> CoroutineScope(Dispatchers.Main).launch { countdown() }
            TrainingState.SERIES -> CoroutineScope(Dispatchers.Main).launch { series() }
            TrainingState.EXERCISE -> CoroutineScope(Dispatchers.Main).launch {
                exercise()
                series()
            }
            TrainingState.REPEAT -> CoroutineScope(Dispatchers.Main).launch {
                if (pendingTime > 0)
                    pendingTime--
                repeat()
                exercise()
                series()
            }
            TrainingState.AFTER_EXERCISES_BREAK -> CoroutineScope(Dispatchers.Main).launch {
                afterExercise()
                exercise()
                series()
            }
            TrainingState.AFTER_SERIES_BREAK -> CoroutineScope(Dispatchers.Main).launch {
                afterSeries()
                series()
            }
            TrainingState.FINISHED -> CoroutineScope(Dispatchers.Main).launch {finish(passed) }
        }
    }

    //TrainingEvent
    private suspend fun countdown() {

        for (i in pendingTime until countdownDuration) {
            setState(TrainingState.COUNTDOWN)

            delay(1000)
            pendingTime++
            log("countdown ${countdownDuration - i}")
            trainingEvent?.onBeforeTrainingCountdownUpdate(countdownDuration - i)
        }
        pendingTime = 0

        series()

        setState(TrainingState.FINISHED)
        passed = true
        finish(passed)

    }

    private suspend fun series() {

        while (currentSeries <= scheduledSeries) {

            setState(TrainingState.SERIES)

            //before series
            delay(1000)
            log("series $currentSeries")
            delay(1000)
            trainingEvent?.onSeriesStart(currentSeries)

            exercise()


            //after not last series

            currentSeries++

            //isn't last series
            if (currentSeries != scheduledSeries + 1) {
                currentExercise = 1
                currentRepeat = 1
                afterSeries()
            }

        }
    }

    private suspend fun exercise() {

        while (currentExercise <= scheduledExercises) {
            setState(TrainingState.EXERCISE)

            delay(1000)
            log("exercise $currentExercise")
            trainingEvent?.onExerciseStart(currentExercise)
            delay(2000)

            repeat()

            currentExercise++

            if (currentExercise != scheduledExercises + 1) {
                currentRepeat = 1
                afterExercise()
            }

        }
    }

    private suspend fun repeat() {

        while (currentRepeat <= scheduledRepeats) {
            setState(TrainingState.REPEAT)

            log("repeat $currentRepeat")
            trainingEvent?.onRepeatStart(currentRepeat)
//            updateRepeatCounter(currentRepeat)
            repeatCountdown()
            if(currentExercise != 5)
                delay(250)
            trainingEvent?.onRepeatEnd(currentRepeat == scheduledRepeats)
            currentRepeat++
            delay(1250)
        }
    }

    private suspend fun repeatCountdown() {
        for (i in pendingTime until repeatDuration) {
            delay(1000)
            trainingEvent?.onRepeatCountdown(pendingTime)
            pendingTime++
//                log("iterator $i")
        }
        pendingTime = 0
    }

    private suspend fun afterExercise() {

        for (i in pendingTime until breakBetweenExercises) {

            setState(TrainingState.AFTER_EXERCISES_BREAK)

            delay(1000)
            pendingTime++
            log("breakBetweenExercises ${breakBetweenExercises - i}")
            trainingEvent?.onBetweenExerciseBreakUpdate(breakBetweenExercises - i)
        }
        pendingTime = 0

    }

    private suspend fun afterSeries() {

        for (i in pendingTime until breakBetweenSeries) {
            setState(TrainingState.AFTER_SERIES_BREAK)

            delay(1000)
            pendingTime++
            log("breakBetweenSeries ${breakBetweenSeries - i}")
            trainingEvent?.onBetweenSeriesBreakUpdate(breakBetweenExercises - i)
        }
        pendingTime = 0
    }


}