package com.yukse.pomodorotimer.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yukse.pomodorotimer.database.DatabaseContract.ToDoEntry

// class의 변수들이 column이 되어 database의 table을 형성
@Entity(tableName = ToDoEntry.TABLE_NAME)
data class ToDoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = ToDoEntry.TITLE) var title: String = "",
    @ColumnInfo(name = ToDoEntry.STUDYTIME) var study: Long = 25,
    @ColumnInfo(name = ToDoEntry.SHORTRESTTIME) var short_rest: Long = 5,
    @ColumnInfo(name = ToDoEntry.LONGRESTTIME) var long_rest: Long = 30,
    @ColumnInfo(name = ToDoEntry.AUTOTIMER) var autoStart: Boolean = false,
    @ColumnInfo(name = ToDoEntry.NOLONGREST) var noLong: Boolean = false,
    @ColumnInfo(name = ToDoEntry.POMO) var pomo: Int = 4

)