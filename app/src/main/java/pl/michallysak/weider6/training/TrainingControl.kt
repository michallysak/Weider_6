package pl.michallysak.weider6.training

interface TrainingControl{

    fun start()

    fun stop()

    fun finish(passed: Boolean)

    fun start(state: TrainingState)

}