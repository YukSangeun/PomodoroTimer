package com.yukse.pomodorotimer

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ToDoListFragment : Fragment() {
    //아이템 클릭 후 타이머로 이동할 때 값 전달할 interface
    //실제 구현은 timer 액티비티에서
    interface OnDataPassLister {
        //fun onDataPass(study: Long?, shortRest: Long?, longRest: Long?, pomo: Int?, auto: Boolean?, noLongRest: Boolean?)
        fun onDataPass(item: Todo)
    }

    private lateinit var dataPassListener: OnDataPassLister
    private val viewModel: ToDoViewModel by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        //context를 ondatapassListener로 형변환 - 데이터 넘겨주기 위해
        dataPassListener = context as OnDataPassLister
    }

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

        val todoView = view.findViewById<RecyclerView>(R.id.rv_todolist)
        val todoAdapter = TodoAdapter(todoView, viewModel.getTodo(), context)
        todoAdapter.setOnItemClickListener(object : TodoAdapter.OnItemClickListener {
            //OnItemClickListener 인터페이스를 통해 전달받는 함수
            // 1. 아이템  삭제/수정 다이얼로그 띄우기
            override fun onItemButtonClick(position: Int) {
                itemEditORDeleteDialog(todoView, position, context)
            }

            // 2. 타이머로 값 전달 후 이동
            override fun onItemTitleClick(position: Int) {
                dataPassListener.onDataPass(viewModel.getTodo()[position])
            }
        })
        with(todoView) {
            this.adapter = todoAdapter
            this.layoutManager = LinearLayoutManager(context)
        }

        //add버튼 눌렀을 때 아이템 추가 - 추가 다이어로그 띄우기
        view.findViewById<Button>(R.id.bt_add).setOnClickListener {
            itemInfoEditDialog(todoView, "할 일 추가", 0, context)
        }

    }

    //수정 or 삭제 다이얼로그 띄우기
    fun itemEditORDeleteDialog(
        todoView: RecyclerView,
        position: Int,
        context: Context?
    ) {
        val eord_list = arrayOf("수정", "삭제")

        AlertDialog.Builder(context)
            .setTitle(viewModel.getTodo()[position].title)
            .setItems(eord_list, DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    0 -> {
                        itemInfoEditDialog(todoView, "할 일 수정", position, context)
                    }
                    1 -> {
                        //삭제
                        viewModel.deleteTodo(position)
                        todoView.adapter?.notifyDataSetChanged()
                    }
                }
            })
            .show()
    }

    //아이템 수정 or 추가 다이얼로그 띄우기
    fun itemInfoEditDialog(
        todoView: RecyclerView,
        title: String,   //dialog title에 적을 문자열
        position: Int,   //edit경우 수정할 아이템의 위치를 알아야한다.
        context: Context?
    ) {
        val addDialogView = LayoutInflater.from(context).inflate(R.layout.todo_item_dialog, null)

        val et_title = addDialogView.findViewById<EditText>(R.id.et_title)
        val et_times = addDialogView.findViewById<EditText>(R.id.et_times)
        val et_study = addDialogView.findViewById<EditText>(R.id.et_study)
        val et_rest = addDialogView.findViewById<EditText>(R.id.et_rest)
        val et_longRest = addDialogView.findViewById<EditText>(R.id.et_long_rest)
        val cb_pomo_auto_run = addDialogView.findViewById<CheckBox>(R.id.cb_pomo_auto_run)
        val cb_no_long_rest = addDialogView.findViewById<CheckBox>(R.id.cb_no_long)

        //기존 값 불러와 화면에 표시
        if (title.equals("할 일 수정")) {
            et_title.setText(viewModel.getTodo()[position].title)
            et_times.setText(viewModel.getTodo()[position].pomoCnt.toString())
            et_study.setText(viewModel.getTodo()[position].studyTime.toString())
            et_rest.setText(viewModel.getTodo()[position].shortRestTime.toString())
            et_longRest.setText(viewModel.getTodo()[position].longRestTime.toString())
            cb_pomo_auto_run.isChecked = viewModel.getTodo()[position].autoStart
            cb_no_long_rest.isChecked = viewModel.getTodo()[position].noLongRest
        } else {   //"할 일 추가"
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            et_times.setText(sp.getInt("long_rest_pomo", 4).toString())
            et_study.setText(sp.getInt("study_time", 1).toString())
            et_rest.setText(sp.getInt("short_rest_time", 1).toString())
            et_longRest.setText(sp.getInt("long_rest_time", 1).toString())
            cb_pomo_auto_run.isChecked = sp.getBoolean("auto_timer", false)
            cb_no_long_rest.isChecked = !(sp.getBoolean("use_long_rest", true))
        }
        //long rest time 사용여부에 따라 다이얼로그 일부분 수정 불가로 변경
        noLongTimer(cb_no_long_rest.isChecked, et_longRest, et_times)

        cb_no_long_rest.setOnClickListener {
            noLongTimer(cb_no_long_rest.isChecked, et_longRest, et_times)
        }
        /* editText 값 변경시 제한 걸기 */
        et_study.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val number = s.toString().toInt()
                if (number > 240) {
                    et_study.setText("240")
                }
            }
        }
        )
        et_rest.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val number = s.toString().toInt()
                if (number > 240) {
                    et_rest.setText("240")
                }
            }
        })
        et_longRest.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val number = s.toString().toInt()
                if (number > 240) {
                    et_longRest.setText("240")
                }
            }
        })
        et_times.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val number = s.toString().toInt()
                if (number > 10) {
                    et_times.setText("10")
                }
            }
        })

        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(addDialogView)
            .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                when (title) {
                    "할 일 추가" -> {
                        viewModel.addTodo(Todo(
                                et_title.text.toString(),
                                Integer.parseInt(et_times.text.toString()),
                                et_study.text.toString().toLong(),
                                et_rest.text.toString().toLong(),
                                et_longRest.text.toString().toLong(),
                                cb_pomo_auto_run.isChecked,
                                cb_no_long_rest.isChecked
                            ))
                    }
                    "할 일 수정" -> {
                        viewModel.editTodo(position, Todo(
                                et_title.text.toString(),
                                Integer.parseInt(et_times.text.toString()),
                                et_study.text.toString().toLong(),
                                et_rest.text.toString().toLong(),
                                et_longRest.text.toString().toLong(),
                                cb_pomo_auto_run.isChecked,
                                cb_no_long_rest.isChecked
                            ))
                    }
                }
                todoView.adapter?.notifyDataSetChanged()

                dialog.dismiss()
            })
            .setNegativeButton("취소", null)
            .show()
    }

//    private fun addTodo(todo: Todo){
//        todo_data.add(todo)
//        todoView.adapter?.notifyDataSetChanged()
//    }
//
//    private fun editTodo(position: Int, todo: Todo){
//        todo_data.set(position, todo)
//        todoView.adapter?.notifyDataSetChanged()
//    }
//
//    private fun deleteTodo(position: Int){
//        todo_data.removeAt(position)
//        todoView.adapter?.notifyDataSetChanged()
//    }

    // long timer를 사용하지 않을 경우 연관된 editText 수정불가로 변경
    fun noLongTimer(isChecked: Boolean, et_longRest: EditText, et_times: EditText) {
        if (isChecked) {
            et_longRest.isFocusable = false
            et_longRest.isClickable = false
            et_longRest.setTextColor(Color.parseColor("#FFFFFF"))
            et_times.isFocusable = false
            et_times.isClickable = false
            et_times.setTextColor(Color.parseColor("#FFFFFF"))
        } else {
            et_longRest.isFocusableInTouchMode = true
            et_longRest.isFocusable = true
            et_longRest.setTextColor(Color.parseColor("#000000"))
            et_times.isFocusableInTouchMode = true
            et_times.isFocusable = true
            et_times.setTextColor(Color.parseColor("#000000"))
        }
    }
}

//recyclerViewAdater
class TodoAdapter(
    val todoView: RecyclerView,
    val itemList: ArrayList<Todo>,
    val context: Context?
) : RecyclerView.Adapter<TodoAdapter.ViewHolder>() {
    // 아이템 클릭시 작동을 위한 인터페이스
    interface OnItemClickListener {
        // 아이템 버튼 클릭하면 수정/삭제 다이어로그 띄우기
        fun onItemButtonClick(position: Int)

        // 아이템 제목 클릭하면 타이머로 이동
        fun onItemTitleClick(position: Int)
    }

    lateinit var itemClickListener: OnItemClickListener

    fun setOnItemClickListener(listener: OnItemClickListener) {
        //fragment에서 아이템 클릭리스너 호출 후 listener 구현할 것
        this.itemClickListener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val todoText: TextView

        init {
            todoText = itemView.findViewById(R.id.tv_todo)
            //item title 눌렀을 때 타이머로 이동
            todoText.setOnClickListener {
                itemClickListener.onItemTitleClick(adapterPosition)
            }

            //item 버튼 눌렀을 때 수정, 삭제 선택 다이어로그 띄우기
            // 수정 선택 시 - 수정 다이어로그
            itemView.findViewById<Button>(R.id.bt_modify).setOnClickListener {
                itemClickListener.onItemButtonClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.todo_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.todoText.setText(itemList.get(position).title)
    }
}