package com.yukse.pomodorotimer.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.yukse.pomodorotimer.database.DatabaseContract.ToDoEntry

//database에 접근할 수 있는 메소드를 포함하며 sql 쿼리를 지정
@Dao
interface ToDoDao {
    // liveData 타입의 형태로 리턴받을 경우, live data 자체가 async하게 동작하므로
    // 별도의 thread에서 호출할 필요없이 main thread에서 호출하여도 에러 없이 수신 가능
    @Query("SELECT * FROM " + ToDoEntry.ITEM_TABLE_NAME)
    fun getAllToDo(): LiveData<List<ToDoEntity>>

    @Query("SELECT * FROM " + ToDoEntry.GROUP_TABLE_NAME)
    fun getAllGroup(): LiveData<List<GroupEntity>>

    @Query("SELECT * FROM " + ToDoEntry.ITEM_TABLE_NAME + " WHERE " + ToDoEntry.GROUP + " = :group_id")
    suspend fun getToDoInGroup(group_id: Int): List<ToDoEntity>

    //suspend: 별도의 thread나 persistance transaction영역에서 실해아지 않아도 되어
    // 편의성이 좋아지고 보일러플레이트 코트들이 줄어든다.
    @Insert
    suspend fun insertToDO(todo: ToDoEntity)

    @Insert
    suspend fun insertGroup(group: GroupEntity)

    @Update
    suspend fun updateToDo(todo: ToDoEntity)

    @Update
    suspend fun updateGroup(group: GroupEntity)

    @Delete
    suspend fun deleteToDo(todo: ToDoEntity?)

    @Query("DELETE FROM " + ToDoEntry.GROUP_TABLE_NAME + " WHERE id = :id")
    suspend fun deleteGroup(id: Int)

}