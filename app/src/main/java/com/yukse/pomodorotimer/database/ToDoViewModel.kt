package com.yukse.pomodorotimer.database

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch

//Todo데이터를 관리
class ToDoViewModel(application: Application) : AndroidViewModel(application) {

    private val toDoRepository: ToDoRepository

    //상태 변경, 관찰이 가능한 liveData를 저장하는 객체
    private val todoLiveData: LiveData<List<ToDoEntity>>
    private val groupLiveData: LiveData<List<GroupEntity>>
    private val todoInGroupData = MutableLiveData<List<ToDoEntity>>()

    init {
        toDoRepository = ToDoRepository(ToDoDatabase.getInstance(application).todoDao())
        todoLiveData = toDoRepository.allToDoList
        groupLiveData = toDoRepository.allGoup
    }

    fun getAllTodo(): List<ToDoEntity>? {
        return todoLiveData.value
    }

    fun getAllGroup(): List<GroupEntity>? {
        return groupLiveData.value
    }

    fun getAllToDoLiveData(): LiveData<List<ToDoEntity>> {
        return todoLiveData
    }

    fun getAllGroupLiveData(): LiveData<List<GroupEntity>> {
        return groupLiveData
    }

    fun setToDoLiveDataInGroup(group_id: Int) = viewModelScope.launch {
        todoInGroupData.value = toDoRepository.getToDoInGroup(group_id)
    }

    fun getToDoLiveDataInGroup(): LiveData<List<ToDoEntity>> {
        return todoInGroupData
    }

    fun addTodo(todo: ToDoEntity) = viewModelScope.launch {
        toDoRepository.insertToDo(todo)
    }

    fun addGroup(group: GroupEntity) = viewModelScope.launch {
        toDoRepository.insertGroup(group)
    }

    fun editTodo(todo: ToDoEntity) = viewModelScope.launch {
        toDoRepository.updateToDo(todo)
    }

    fun editGroup(group: GroupEntity) = viewModelScope.launch {
        toDoRepository.updateGroup(group)
    }

    fun deleteTodo(todo: ToDoEntity?) = viewModelScope.launch {
        toDoRepository.deleteToDo(todo)
    }

    fun deleteGroup(position: Int) = viewModelScope.launch {
        toDoRepository.deleteGroup(position)
    }
}