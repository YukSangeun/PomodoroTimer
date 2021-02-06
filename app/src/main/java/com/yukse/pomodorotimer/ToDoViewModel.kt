package com.yukse.pomodorotimer

import androidx.lifecycle.ViewModel

//Todo데이터를 관리
class ToDoViewModel : ViewModel() {
    private val todo_data = arrayListOf<Todo>()

    fun getTodo(): ArrayList<Todo>{
        return todo_data
    }

    fun addTodo(todo: Todo) {
        todo_data.add(todo)
    }

    fun editTodo(position: Int, todo: Todo) {
        todo_data.set(position, todo)
    }

    fun deleteTodo(position: Int) {
        todo_data.removeAt(position)
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