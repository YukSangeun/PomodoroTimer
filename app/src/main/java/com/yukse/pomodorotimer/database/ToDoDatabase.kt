package com.yukse.pomodorotimer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// 데이터베이스의 holder를 구성하며 지속적인 관계형 데이터의 기본 연결을 위한 access point 역할을 한다
// database annotation의 version: 신규 변경사항이 생길 경우 version을 올려준다
// 또한, 이전 버전과 달라진 점들에 대해 어떻게 변경사항을 집어넣을 것인지 migration 클래스를 작성해 적용해야 한다.
// exportSchema=false 해당 옵션을 true로 하게 되면 빌드시에 테이블 생성에 관련된 쿼리 및 Table Column 정보를 json 파일로 생성하여 프로젝트 내에 생성
@Database(entities = [ToDoEntity::class], version = 1, exportSchema = false)
abstract class ToDoDatabase : RoomDatabase() {
    abstract fun todoDao(): ToDoDao

    //단일 프로세스에서 실행될 경우 싱글톤 디자인 패턴에 따라 객체 인스턴스화
    // 생산 비용 많이드므로
    // 싱글톤 패턴: 어디서든 접근가능하고 중복 생성되지 않는 패턴
    companion object {
        private var instance: ToDoDatabase? = null

        @Synchronized
        fun getInstance(context: Context): ToDoDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    ToDoDatabase::class.java,
                    DatabaseContract.DB_NAME.DATABASE_NAME
                )
                    .allowMainThreadQueries()
                    .build()
            }
            return instance!!
        }
    }
}