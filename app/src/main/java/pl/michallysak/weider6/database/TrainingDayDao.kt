package pl.michallysak.weider6.database

import androidx.room.*


@Dao
interface TrainingDayDao {

    @get:Query("SELECT * FROM training_table")
    val all: List<TrainingDay>

    @get:Query("SELECT COUNT(*) FROM training_table")
    val size: Int

    @get:Query("SELECT * FROM training_table WHERE passed = 0 LIMIT 1")
    val lastPassedTrainingDay: TrainingDay

    @Query("SELECT * FROM training_table WHERE day = :day")
    fun getDay(day: Int): TrainingDay

    @Insert
    fun insertAll(trainingDays: List<TrainingDay>)

    @Update
    fun update(trainings: TrainingDay)

    @Delete
    fun deleteAll(trainingDays: List<TrainingDay>)

}