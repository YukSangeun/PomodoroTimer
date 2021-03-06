package com.yukse.pomodorotimer.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

//버전 업그레이드 - group 테이블 추가, 기존 테이블에 group 컬럼 추가
//기존 데이터를 유지한채 변경된 사항을 적용할 경우 migration 사용
val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE " + DatabaseContract.GROUP_TABLE_NAME +
                    " (id INTEGER not null primary key autoincrement, " +
                    DatabaseContract.GROUP + " TEXT  not null)"
        )
        database.execSQL(
            "ALTER TABLE " + DatabaseContract.ITEM_TABLE_NAME +
                    " ADD COLUMN " + DatabaseContract.GROUP + " INTEGER not null default 1 references " +
                    DatabaseContract.GROUP_TABLE_NAME + "(id) on delete cascade"
        )
    }
}

// 데이터베이스의 holder를 구성하며 지속적인 관계형 데이터의 기본 연결을 위한 access point 역할을 한다
// database annotation의 version: 신규 변경사항이 생길 경우 version을 올려준다
// 또한, 이전 버전과 달라진 점들에 대해 어떻게 변경사항을 집어넣을 것인지 migration 클래스를 작성해 적용해야 한다.
// exportSchema=false 해당 옵션을 true로 하게 되면 빌드시에 테이블 생성에 관련된 쿼리 및 Table Column 정보를 json 파일로 생성하여 프로젝트 내에 생성
@Database(entities = [ToDoEntity::class, GroupEntity::class], version = 2, exportSchema = false)
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
                Log.d("txx", "데이터베이스 생성")
                // fallbackToDestructiveMigration() 은 기존 데이터를 reset하고 변경된 사항적용해서 새로 시작하겠다는 뜻
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    ToDoDatabase::class.java,
                    DatabaseContract.DB_NAME.DATABASE_NAME
                )
                    .addCallback(object : Callback(){
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d("txx", "초기 데이터 생성")
                            db.execSQL("insert into " + DatabaseContract.GROUP_TABLE_NAME +
                                    "(" + DatabaseContract.GROUP + ") values ('나의 작업')")
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            Log.d("txx", "on open")
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return instance!!
        }
    }
}