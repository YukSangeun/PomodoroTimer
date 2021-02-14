package com.yukse.pomodorotimer

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yukse.pomodorotimer.database.ToDoEntity
import com.yukse.pomodorotimer.databinding.TodoItemViewBinding

//recyclerViewAdater
class TodoAdapter(
    private var todoList: List<ToDoEntity>,
    private val context: Context?
) : RecyclerView.Adapter<TodoAdapter.ViewHolder>() {
    // 아이템 클릭시 작동을 위한 인터페이스
    interface OnItemClickListener {
        // 아이템 버튼 클릭하면 수정/삭제 다이어로그 띄우기
        fun onItemButtonClick(position: Int)

        // 아이템 제목 클릭하면 타이머로 이동
        fun onItemTitleClick(position: Int)
    }

    private lateinit var itemClickListener: OnItemClickListener

    fun setOnItemClickListener(listener: OnItemClickListener) {
        //fragment에서 아이템 클릭리스너 호출 후 listener 구현할 것
        this.itemClickListener = listener
    }

    inner class ViewHolder(val itemViewBinding: TodoItemViewBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        init {
            //item title 눌렀을 때 타이머로 이동
            itemViewBinding.tvTodo.setOnClickListener {
                itemClickListener.onItemTitleClick(adapterPosition)
            }

            //item 버튼 눌렀을 때 수정, 삭제 선택 다이어로그 띄우기
            // 수정 선택 시 - 수정 다이어로그
            itemViewBinding.btModify.setOnClickListener {
                itemClickListener.onItemButtonClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.todo_item_view, parent, false)
        return ViewHolder(TodoItemViewBinding.bind(view))
    }

    override fun getItemCount(): Int {
        return todoList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemViewBinding.tvTodo.setText(todoList.get(position).title)
    }

    //live data를 이용한 데이터 갱신을 위해 구현 - 이 함수 호출하면 데이터 바뀌도록.
    internal fun setData(newData: List<ToDoEntity>) {
        todoList = newData
        notifyDataSetChanged()
    }
}