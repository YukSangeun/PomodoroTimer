package com.yukse.pomodorotimer.database

import android.provider.BaseColumns

class DatabaseContract {
    object DB_NAME {
        const val DATABASE_NAME = "todo.db"
    }

    // contract class를 인스턴트화 하는 것을 막기 위해 private
    private constructor() {}

    //table content 정의하는 이너클래스
    companion object ToDoEntry : BaseColumns {
        const val TABLE_NAME: String = "todo"
        const val TITLE: String = "column_title"
        const val STUDYTIME: String = "column_study"
        const val SHORTRESTTIME: String = "column_short_rest"
        const val LONGRESTTIME: String = "column_long_rest"
        const val AUTOTIMER: String = "column_auto"
        const val NOLONGREST: String = "column_no_long"
        const val POMO: String = "pomo"
    }

}