package pl.michallysak.weider6.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "training_table")
class TrainingDay(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "day") val day: Int,
    @ColumnInfo(name = "series") val series: Int,
    @ColumnInfo(name = "repeat") val repeat: Int,
    @ColumnInfo(name = "passed") var passed: Int

)