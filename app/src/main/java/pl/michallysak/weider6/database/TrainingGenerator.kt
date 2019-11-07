package pl.michallysak.weider6.database

fun generateTraining(days: Int): List<TrainingDay> {
    val trainingDays = mutableListOf<TrainingDay>()

    var repeat = 6

    //first 6 days -> const
    for (i in 1 until 4)
        for (j in 0 until i%5)
            trainingDays.add(TrainingDay(null, i + j, i , repeat, 0))

    var increaseRepeat = 1

    //another days -> increase every 4 day
    for (i in 7 until days + 1) {

        if (increaseRepeat % 4 == 0) {
            repeat += 2
            increaseRepeat = 1
        }else
            increaseRepeat++

        trainingDays.add(TrainingDay(null, i, 3, repeat, 0))
    }

    return trainingDays
}