package com.yukse.pomodorotimer

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ToDoListFragment : Fragment(){

    private val todo_data = arrayListOf<Todo>()

    //뷰를 그리는 라이프사이클
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("life_cycle", "onCreateView")

        //프래그먼트가 인터페이스를 처음으로 그릴 때 호출된다.
        // inflater : 뷰를 그려주는 역할
        // container : 부모 뷰
        //inflate의 반환값이 view이므로 그대로 반환해주면 됨.
        return inflater.inflate(R.layout.todolist_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //임시로 데이터 넣기
        todo_data.add(Todo(text = "UI 만들기"))
        todo_data.add(Todo(text = "9시 기상하기!!!"))

        val todoAdapter = TodoAdapter(todo_data, LayoutInflater.from(context))
        val todoView = view.findViewById<RecyclerView>(R.id.rv_todolist)
        with(todoView){
            this.adapter = todoAdapter
            this.layoutManager = LinearLayoutManager(context)
        }
    }
}

//recyclerViewAdater
class TodoAdapter(
    val itemList: ArrayList<Todo>,
    val inflater: LayoutInflater
): RecyclerView.Adapter<TodoAdapter.ViewHolder>(){

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val todoText: CheckBox
        init {
            todoText = itemView.findViewById(R.id.checkbox_todo)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.todo_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.todoText.setText(itemList.get(position).text)
    }
}

//To do
data class Todo(var text: String, var isDone: Boolean=false, var pomoCnt: Int=4, var studyTime: Int=25, var restTime: Int=5)