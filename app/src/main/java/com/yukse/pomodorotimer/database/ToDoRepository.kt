package com.yukse.pomodorotimer.database

import androidx.lifecycle.LiveData

//데이터베이스에 접근할 수 있는 중재자 역할
// data operation을 handling 하는 책임이 있어 앱에 명확한 API를 제공
// 이를 통해, data를 추상화하여 view model에서 data가 어떻게 fetch 되었는지 알 피룡가 없도록 한다.
class ToDoRepository(private val toDoDao: ToDoDao) {

    val allToDoList: LiveData<List<ToDoEntity>> = getAllToDo()
    val allGoup: LiveData<List<GroupEntity>> = getAllGroup()

    fun getAllToDo(): LiveData<List<ToDoEntity>> {
        return toDoDao.getAllToDo()
    }

    fun getAllGroup(): LiveData<List<GroupEntity>>{
        return toDoDao.getAllGroup()
    }

    fun getToDoInGroup(group_id: Int): LiveData<List<ToDoEntity>>{
        return toDoDao.getToDoInGroup(group_id)
    }

    suspend fun insertToDo(toDoEntity: ToDoEntity) {
        toDoDao.insertToDO(toDoEntity)
    }

    suspend fun insertGroup(groupEntity: GroupEntity){
        toDoDao.insertGroup(groupEntity)
    }

    suspend fun updateToDo(toDoEntity: ToDoEntity) {
        toDoDao.updateToDo(toDoEntity)
    }

    suspend fun updateGroup(groupEntity: GroupEntity){
        toDoDao.updateGroup(groupEntity)
    }

    suspend fun deleteToDo(toDoEntity: ToDoEntity) {
        toDoDao.deleteToDo(toDoEntity)
    }

    suspend fun deleteGroup(groupEntity: GroupEntity){
        toDoDao.deleteGroup(groupEntity)
    }

}