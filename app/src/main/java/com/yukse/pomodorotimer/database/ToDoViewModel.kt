package com.yukse.pomodorotimer.database

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

//Todo데이터를 관리
class ToDoViewModel(application: Application) : AndroidViewModel(application) {

    private val toDoRepository: ToDoRepository

    //상태 변경, 관찰이 가능한 liveData를 listTodo 라는 타입으로 저장하는 객체
    private val todoLiveData: LiveData<List<ToDoEntity>>
    private val groupLiveData: LiveData<List<GroupEntity>>

    init {
        val todoDao = ToDoDatabase.getInstance(application).todoDao()
        toDoRepository = ToDoRepository(todoDao)
        todoLiveData = toDoRepository.allToDoList
        groupLiveData = toDoRepository.allGoup

        //group에 기본적으로 '내 목록' 추가되어 있도록
        addGroup(GroupEntity(group = "나의 목록"))
    }

    fun getTodo(): List<ToDoEntity>? {
        return todoLiveData.value
    }

    fun getGroup(): List<GroupEntity>? {
        return groupLiveData.value
    }

    fun getToDoLiveData(): LiveData<List<ToDoEntity>> {
        return todoLiveData
    }

    fun getGroupLiveData(): LiveData<List<GroupEntity>>{
        return groupLiveData
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

    fun deleteTodo(position: Int) = viewModelScope.launch {
        toDoRepository.deleteToDo(todoLiveData.value!!.get(position))
    }

    fun deleteGroup(position: Int) = viewModelScope.launch {
        toDoRepository.deleteGroup(groupLiveData.value!!.get(position))
    }
}