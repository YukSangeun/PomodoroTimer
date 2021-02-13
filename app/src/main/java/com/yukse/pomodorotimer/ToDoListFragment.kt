package com.yukse.pomodorotimer

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yukse.pomodorotimer.database.GroupEntity
import com.yukse.pomodorotimer.database.ToDoEntity
import com.yukse.pomodorotimer.database.ToDoViewModel
import com.yukse.pomodorotimer.databinding.*

class ToDoListFragment : Fragment() {
    //아이템 클릭 후 타이머로 이동할 때 값 전달할 interface
    //실제 구현은 timer 액티비티에서
    interface OnDataPassLister {
        fun onDataPass(item: ToDoEntity)
    }

    private var _binding: TodolistFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var dataPassListener: OnDataPassLister
    private lateinit var viewModel: ToDoViewModel

    //환경설정 변수
    private lateinit var sp: SharedPreferences

    //현재 화면에 나타나는 그룹
    private var current_group: Int = 0

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

        //toolbar 의 menu 설정 가능하도록 설정
        setHasOptionsMenu(true)

        //프래그먼트가 인터페이스를 처음으로 그릴 때 호출된다.
        // inflater : 뷰를 그려주는 역할
        // container : 부모 뷰
        //inflater.inflate의 반환값이 view이므로 그대로 반환해주면 됨.
        _binding = TodolistFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //환경설정 가져오기
        sp = PreferenceManager.getDefaultSharedPreferences(context)

        // view model 생성
        viewModel = ViewModelProvider(this).get(ToDoViewModel::class.java)

        val todoAdapter = TodoAdapter(emptyList(), context)
        todoAdapter.setOnItemClickListener(object : TodoAdapter.OnItemClickListener {
            //OnItemClickListener 인터페이스를 통해 전달받는 함수
            // 1. 아이템  삭제/수정 다이얼로그 띄우기
            override fun onItemButtonClick(position: Int) {
                itemEditORDeleteDialog(position)
            }

            // 2. 타이머로 값 전달 후 이동
            override fun onItemTitleClick(position: Int) {
                dataPassListener.onDataPass(viewModel.getToDoLiveDataInGroup().value!!.get(position))
            }
        })
        with(binding.rvTodolist) {
            this.adapter = todoAdapter
            this.layoutManager = LinearLayoutManager(context)
        }

        binding.tvGroup.setOnClickListener {
            groupListDialog()
        }

        //========== live data observer 설정 ====================
        viewModel.getFirstRowGroup().observe(viewLifecycleOwner, Observer {
            // current_group이 null인 경우(초기 화면 생성시)만 변경하도록
            if (current_group == 0) {
                current_group = it.id
                Log.d("txx", "current_group: " + current_group)
                binding.tvGroup.setText(it.group)
            }
        })
        //현재 그룹에 있는 list를 UI에 표시
        viewModel.getToDoLiveDataInGroup().observe(viewLifecycleOwner, Observer {
            Log.d("txx", "리스트 표시: " + it)
            (binding.rvTodolist.adapter as TodoAdapter).setData(it)
        })
        // 데이터 변경사항 있을 때마다 UI 업데이트
        viewModel.getAllToDoLiveData().observe(viewLifecycleOwner, Observer {
            viewModel.setToDoLiveDataInGroup(current_group)
            Log.d("txx", "todo live data: " + viewModel.getAllTodo())
        })
        viewModel.getAllGroupLiveData().observe(viewLifecycleOwner, Observer {
            Log.d("txx", "group: " + it)
        })
    }

    // menu 선택별 action 지정
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                itemInfoEditDialog("할 일 추가", 0, context)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //그룹 리스트 다이얼로그 띄우기
    fun groupListDialog() {
        val dialogBinding = GroupDialogBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.dialogTitle.setText("그룹 목록")
        viewModel.getAllGroupNameData().observe(viewLifecycleOwner, Observer {
            Log.d("txx", "group name" + it)
            dialogBinding.lvSelectAction.adapter =
                ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, it)
        })
        dialogBinding.btGroupAdd.setOnClickListener {
            val et_group_name = EditText(context)
            val addDialog = AlertDialog.Builder(context)
                .setTitle("그룹 추가")
                .setView(et_group_name)
                .setPositiveButton("확인", null)
                .setNegativeButton("취소", null)
                .create()

            addDialog.setOnShowListener(object : DialogInterface.OnShowListener {
                override fun onShow(dialog: DialogInterface?) {
                    addDialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener {
                        if (et_group_name.text.isNullOrEmpty()) {
                            Toast.makeText(context, "빈 칸을 채워주세요.", Toast.LENGTH_SHORT).show()
                        } else{
                            viewModel.addGroup(GroupEntity(group = et_group_name.text.toString()))
                            addDialog.dismiss()
                        }
                    }
                }
            })
            addDialog.show()
        }
        dialogBinding.lvSelectAction.setOnItemClickListener { parent, view, position, id ->
            current_group = viewModel.getAllGroup()!![position].id
            Log.d("txx", "click " + current_group)
            binding.tvGroup.setText(viewModel.getAllGroup()?.get(position)?.group)
            viewModel.setToDoLiveDataInGroup(current_group)
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    //수정 or 삭제 다이얼로그 띄우기
    fun itemEditORDeleteDialog(
        item_pos: Int
    ) {
        val item_id = viewModel.getToDoLiveDataInGroup().value!!.get(item_pos).id
        Log.d("txx", "item position: " + item_pos)
        Log.d("txx", "item id: " + item_id)
        val eord_list = arrayOf("수정", "삭제")

        val dialogBinding = TodoItemActionDialogBinding.inflate(LayoutInflater.from(context))

        dialogBinding.dialogTitle.setText(viewModel.getToDoLiveDataInGroup().value!!.get(item_pos).title)
        dialogBinding.lvSelectAction.adapter =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, eord_list)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.lvSelectAction.setOnItemClickListener { parent, view, position, id ->
            when (position) {
                0 -> {
                    itemInfoEditDialog("할 일 수정", item_pos, context)
                }
                1 -> {
                    //삭제
                    viewModel.deleteTodo(viewModel.getToDoLiveDataInGroup().value?.get(item_pos))
                }
            }
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    //아이템 수정 or 추가 다이얼로그 띄우기
    fun itemInfoEditDialog(
        title: String,   //dialog title에 적을 문자열
        position: Int,   //edit경우 수정할 아이템의 위치를 알아야한다.
        context: Context?
    ) {
        val dialogBinding = TodoItemDialogBinding.inflate(LayoutInflater.from(context))

        dialogBinding.dialogTitle.setText(title)
        //기존 값 불러와 화면에 표시
        if (title.equals("할 일 수정")) {
            dialogBinding.etTitle.setText(viewModel.getToDoLiveDataInGroup().value!!.get(position).title)
            dialogBinding.etTimes.setText(viewModel.getToDoLiveDataInGroup().value!!.get(position).pomo.toString())
            dialogBinding.etStudy.setText(viewModel.getToDoLiveDataInGroup().value!!.get(position).study.toString())
            dialogBinding.etRest.setText(viewModel.getToDoLiveDataInGroup().value!!.get(position).short_rest.toString())
            dialogBinding.etLongRest.setText(viewModel.getToDoLiveDataInGroup().value!!.get(position).long_rest.toString())
            dialogBinding.cbPomoAutoRun.isChecked =
                viewModel.getToDoLiveDataInGroup().value!!.get(position).autoStart
            dialogBinding.cbNoLong.isChecked =
                viewModel.getToDoLiveDataInGroup().value!!.get(position).noLong
        } else {   //"할 일 추가"
            dialogBinding.etTimes.setText(sp.getInt("long_rest_pomo", 4).toString())
            dialogBinding.etStudy.setText(sp.getInt("study_time", 25).toString())
            dialogBinding.etRest.setText(sp.getInt("short_rest_time", 5).toString())
            dialogBinding.etLongRest.setText(sp.getInt("long_rest_time", 30).toString())
            dialogBinding.cbPomoAutoRun.isChecked = sp.getBoolean("auto_timer", false)
            dialogBinding.cbNoLong.isChecked = !(sp.getBoolean("use_long_rest", true))
        }
        //long rest time 사용여부에 따라 다이얼로그 일부분 수정 불가로 변경
        noLongTimer(dialogBinding)

        dialogBinding.cbNoLong.setOnClickListener {
            noLongTimer(dialogBinding)
        }
        /* editText 값 변경시 제한 걸기 */
        dialogBinding.etStudy.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val number = s.toString()
                if (number != "") {
                    if (number.toInt() > 240) {
                        dialogBinding.etStudy.setText("240")
                    }
                }
            }
        }
        )
        dialogBinding.etRest.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val number = s.toString()
                if (number != "") {
                    if (number.toInt() > 240) {
                        dialogBinding.etRest.setText("240")
                    }
                }
            }
        })
        dialogBinding.etLongRest.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val number = s.toString()
                if (number != "") {
                    if (number.toInt() > 240) {
                        dialogBinding.etLongRest.setText("240")
                    }
                }
            }
        })
        dialogBinding.etTimes.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val number = s.toString()
                if (number != "") {
                    if (number.toInt() > 10) {
                        dialogBinding.etTimes.setText("10")
                    }
                }
            }
        })

        val dialog = AlertDialog.Builder(context)
            .setView(dialogBinding.root)
            .create()
        //dialog show시 이벤트
        //dialog의 버튼 listener 바꾸기. 클릭시 자동을 dismiss()호출 방지
        dialog.setOnShowListener(object : DialogInterface.OnShowListener {
            override fun onShow(dialog: DialogInterface?) {
                dialogBinding.btCancle.setOnClickListener {
                    dialog?.dismiss()
                }
                dialogBinding.btOk.setOnClickListener {
                    //빈칸이 존재할 경우 toast 띄우기
                    if (dialogBinding.etTitle.text.toString()
                            .equals("") || dialogBinding.etStudy.text.toString()
                            .equals("") || dialogBinding.etRest.text.toString()
                            .equals("") || (!dialogBinding.cbNoLong.isChecked && (dialogBinding.etLongRest.text.toString()
                            .equals("") || dialogBinding.etTimes.text.toString().equals("")))
                    ) {
                        Toast.makeText(context, "빈 칸을 채워주세요.", Toast.LENGTH_SHORT).show()
                    } else {
                        when (title) {
                            "할 일 추가" -> {
                                addToDo(
                                    title = dialogBinding.etTitle.text.toString(),
                                    study = dialogBinding.etStudy.text.toString(),
                                    short_rest = dialogBinding.etRest.text.toString(),
                                    long_rest = dialogBinding.etLongRest.text.toString(),
                                    pomo = dialogBinding.etTimes.text.toString(),
                                    autoStart = dialogBinding.cbPomoAutoRun.isChecked,
                                    noLong = dialogBinding.cbNoLong.isChecked
                                )
                            }
                            "할 일 수정" -> {
                                editToDo(
                                    id = viewModel.getToDoLiveDataInGroup().value!!.get(position).id,
                                    title = dialogBinding.etTitle.text.toString(),
                                    study = dialogBinding.etStudy.text.toString(),
                                    short_rest = dialogBinding.etRest.text.toString(),
                                    long_rest = dialogBinding.etLongRest.text.toString(),
                                    pomo = dialogBinding.etTimes.text.toString(),
                                    autoStart = dialogBinding.cbPomoAutoRun.isChecked,
                                    noLong = dialogBinding.cbNoLong.isChecked
                                )
                            }
                        }
                        dialog?.dismiss()
                    }
                }
            }
        })
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

    }

    // long timer를 사용하지 않을 경우 연관된 editText 수정불가로 변경
    fun noLongTimer(dialogBinding: TodoItemDialogBinding) {
        if (dialogBinding.cbNoLong.isChecked) {
            dialogBinding.etLongRest.isFocusable = false
            dialogBinding.etLongRest.isClickable = false
            dialogBinding.etLongRest.setTextColor(Color.parseColor("#FFFFFF"))
            dialogBinding.etTimes.isFocusable = false
            dialogBinding.etTimes.isClickable = false
            dialogBinding.etTimes.setTextColor(Color.parseColor("#FFFFFF"))
        } else {
            dialogBinding.etLongRest.isFocusableInTouchMode = true
            dialogBinding.etLongRest.isFocusable = true
            dialogBinding.etLongRest.setTextColor(Color.parseColor("#000000"))
            dialogBinding.etTimes.isFocusableInTouchMode = true
            dialogBinding.etTimes.isFocusable = true
            dialogBinding.etTimes.setTextColor(Color.parseColor("#000000"))
        }
    }

    fun addToDo(
        title: String,
        study: String,
        short_rest: String,
        long_rest: String,
        pomo: String,
        noLong: Boolean,
        autoStart: Boolean
    ) {
        // long rest를 사용하지 않을 경우, long time 과 pomo를 빈칸으로 남겨둘 때 에러 발생 방지
        val long_ =
            if (long_rest == "") sp.getInt("long_rest_time", 30).toLong() else long_rest.toLong()
        val pomo_ = if (pomo == "") sp.getInt("long_rest_pomo", 4) else pomo.toInt()

        viewModel.addTodo(
            ToDoEntity(
                group = current_group,
                title = title,
                pomo = pomo_,
                study = study.toLong(),
                short_rest = short_rest.toLong(),
                long_rest = long_,
                autoStart = autoStart,
                noLong = noLong
            )
        )
    }

    fun editToDo(
        id: Int,
        title: String,
        study: String,
        short_rest: String,
        long_rest: String,
        pomo: String,
        noLong: Boolean,
        autoStart: Boolean
    ) {
        // long rest를 사용하지 않을 경우, long time 과 pomo를 빈칸으로 남겨둘 때 에러 발생 방지
        val long_ =
            if (long_rest == "") sp.getInt("long_rest_time", 30).toLong() else long_rest.toLong()
        val pomo_ = if (pomo == "") sp.getInt("long_rest_pomo", 4) else pomo.toInt()

        viewModel.editTodo(
            ToDoEntity(
                id = id,
                group = current_group,
                title = title,
                pomo = pomo_,
                study = study.toLong(),
                short_rest = short_rest.toLong(),
                long_rest = long_,
                autoStart = autoStart,
                noLong = noLong
            )
        )
    }
}

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