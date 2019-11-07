package pl.michallysak.weider6.database

import androidx.room.RoomDatabase
import androidx.room.Database


@Database(entities = [TrainingDay::class], version = 1)
abstract class TrainingDayDatabase : RoomDatabase() {
    abstract fun trainingDao(): TrainingDayDao
}