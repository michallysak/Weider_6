package pl.michallysak.weider6.training


interface TrainingEvent {

    fun onTrainingStart(scheduledSeries: Int, scheduledExercises: Int, scheduledRepeats: Int)

    fun onBeforeTrainingCountdownUpdate(timeToTraining: Int)

    fun onSeriesStart(seriesNumber: Int)

    fun onBetweenSeriesBreakUpdate(timeToNewSeries: Int)

    fun onExerciseStart(exerciseNumber: Int)

    fun onBetweenExerciseBreakUpdate(timeToNewExercise: Int)

    fun onRepeatStart(repeatNumber: Int)

    fun onRepeatEnd(lastRepeat: Boolean)

    fun onRepeatCountdown(countDownTime: Int)

    fun onTrainingEnd(passed: Boolean)
}