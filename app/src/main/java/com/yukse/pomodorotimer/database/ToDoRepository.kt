package com.yukse.pomodorotimer.database

import androidx.lifecycle.LiveData

//데이터베이스에 접근할 수 있는 중재자 역할
// data operation을 handling 하는 책임이 있어 앱에 명확한 API를 제공
// 이를 통해, data를 추상화하여 view model에서 data가 어떻게 fetch 되었는지 알 피룡가 없도록 한다.
class ToDoRepository(private val toDoDao: ToDoDao) {

    val allToDoList: LiveData<List<ToDoEntity>> = getAllToDo()

    fun getAllToDo(): LiveData<List<ToDoEntity>> {
        return toDoDao.getAll()
    }

    suspend fun insertToDo(toDoEntity: ToDoEntity) {
        toDoDao.insertToDO(toDoEntity)
    }

    suspend fun updateToDo(toDoEntity: ToDoEntity) {
        val ret = toDoDao.updateToDo(toDoEntity)
    }

    suspend fun deleteToDo(toDoEntity: ToDoEntity) {
        toDoDao.deleteToDo(toDoEntity)
    }


}