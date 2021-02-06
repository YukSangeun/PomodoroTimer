package com.yukse.pomodorotimer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

//Todo데이터를 관리
class ToDoViewModel : ViewModel() {
    //상태 변경, 관찰이 가능한 liveData를 listTodo 라는 타입으로 저장하는 객체
    val todoLiveData = MutableLiveData<List<Todo>>()
    private val todo_data = arrayListOf<Todo>()

    fun getTodo(): ArrayList<Todo>{
        return todo_data
    }

    fun addTodo(todo: Todo) {
        todo_data.add(todo)
        //변경된 최신 데이터로 liveData를 변경해줌
        todoLiveData.value = todo_data
    }

    fun editTodo(position: Int, todo: Todo) {
        todo_data.set(position, todo)
        todoLiveData.value = todo_data
    }

    fun deleteTodo(position: Int) {
        todo_data.removeAt(position)
        todoLiveData.value = todo_data
    }
}

//To do
data class Todo(
    var title: String,
    var pomoCnt: Int = 4,
    var studyTime: Long = 25,
    var shortRestTime: Long = 5,
    var longRestTime: Long = 30,
    var autoStart: Boolean = false,
    var noLongRest: Boolean = false
)