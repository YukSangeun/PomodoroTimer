package com.yukse.pomodorotimer.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

//Todo데이터를 관리
class ToDoViewModel(application: Application) : AndroidViewModel(application) {

    private val toDoRepository: ToDoRepository

    //상태 변경, 관찰이 가능한 liveData를 listTodo 라는 타입으로 저장하는 객체
    private val todoLiveData: LiveData<List<ToDoEntity>>

    init {
        val todoDao = ToDoDatabase.getInstance(application).todoDao()
        toDoRepository = ToDoRepository(todoDao)
        todoLiveData = toDoRepository.allToDoList
    }

    fun getTodo(): List<ToDoEntity>? {
        return todoLiveData.value
    }

    fun getLiveData(): LiveData<List<ToDoEntity>> {
        return todoLiveData
    }

    fun addTodo(todo: ToDoEntity) = viewModelScope.launch {
        toDoRepository.insertToDo(todo)
    }

    fun editTodo(todo: ToDoEntity) = viewModelScope.launch {
        toDoRepository.updateToDo(todo)
    }

    fun deleteTodo(position: Int) = viewModelScope.launch {
        toDoRepository.deleteToDo(todoLiveData.value!!.get(position))
    }
}