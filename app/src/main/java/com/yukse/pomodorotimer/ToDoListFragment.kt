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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
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
    private var _groupDialogBinding: GroupDialogBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val groupDialogBinding get() = _groupDialogBinding!!
    private lateinit var groupDialog: AlertDialog
    private lateinit var dataPassListener: OnDataPassLister
    private lateinit var viewModel: ToDoViewModel

    //환경설정 변수
    private lateinit var sp: SharedPreferences

    //현재 화면에 나타나는 그룹
    private var current_group_id: Int = 0
    private var current_group_name: String = ""

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
        _groupDialogBinding = GroupDialogBinding.inflate(inflater, container, false)
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

        binding.groupTitle.setOnClickListener {
            groupDialog.show()
        }

        // group list dialog 기본설정 - live data사용할 것
        setGroupListDialog()

        //========== live data observer 설정 ====================
        //현재 그룹에 있는 list를 UI에 표시
        viewModel.getToDoLiveDataInGroup().observe(viewLifecycleOwner, Observer {
            Log.d("txx", "리스트 표시: " + it)
            (binding.rvTodolist.adapter as TodoAdapter).setData(it)
        })
        // 데이터 변경사항 있을 때마다 UI 업데이트
        viewModel.getAllToDoLiveData().observe(viewLifecycleOwner, Observer {
            Log.d("txx", "todo Live data: " + current_group_id)
            if (current_group_id != 0)
                viewModel.setToDoLiveDataInGroup(current_group_id)
            Log.d("txx", "todo live data: " + viewModel.getAllTodo())
        })
        viewModel.getAllGroupLiveData().observe(viewLifecycleOwner, Observer {
            Log.d("txx", "group: " + it)
            (groupDialogBinding.rvGrouplist.adapter as GroupAdapter).setData(it)
            if (current_group_id == 0) {
                current_group_id = it[0].id
                current_group_name = it[0].group
                viewModel.setToDoLiveDataInGroup(current_group_id)
                binding.tvGroup.setText(current_group_name)
                Log.d("txx", "first row")
                Log.d("txx", "current_group: " + current_group_id)
            } else binding.tvGroup.setText(current_group_name)
        })
    }

    // menu 선택별 action 지정
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                itemInfoEditDialog("작업 추가", 0, context)
            }
            R.id.action_edit -> {
                groupAddOREditDialog("이름 변경")
            }
            R.id.action_delete -> {
                if (viewModel.getAllGroup()?.size == 1) {
                    AlertDialog.Builder(context)
                        .setMessage("2개 이상의 목록이 존재할 경우 삭제 가능합니다.")
                        .setPositiveButton("확인", null)
                        .show()
                } else {
                    AlertDialog.Builder(context)
                        .setMessage("이 목록의 모든 타이머가 삭제됩니다.")
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                            viewModel.deleteGroup(current_group_id)
                            current_group_id = 0
                        })
                        .setNegativeButton("취소", null)
                        .show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _groupDialogBinding = null
    }

    //그룹 리스트 다이얼로그 띄우기
    fun setGroupListDialog() {
        groupDialog = AlertDialog.Builder(context)
            .setView(groupDialogBinding.root)
            .create()

        val groupAdapter = GroupAdapter(emptyList(), context)
        groupAdapter.setOnGroupClickListener(object : GroupAdapter.OnGroupClickListener {
            override fun onGroupClick(position: Int) {
                current_group_id = viewModel.getAllGroup()!![position].id
                Log.d("txx", "click " + current_group_id)
                current_group_name = viewModel.getAllGroup()!![position].group
                binding.tvGroup.setText(current_group_name)
                viewModel.setToDoLiveDataInGroup(current_group_id)
                groupDialog.dismiss()
            }
        })
        groupDialogBinding.rvGrouplist.adapter = groupAdapter
        groupDialogBinding.rvGrouplist.layoutManager = LinearLayoutManager(context)
        groupDialogBinding.rvGrouplist.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager(context).orientation
            )
        )
        groupDialogBinding.btGroupAdd.setOnClickListener {
            groupAddOREditDialog("목록 추가")
        }
        groupDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun groupAddOREditDialog(
        title: String
    ) {
        val setGroupNameDialogBinding = SetGroupNameDialogBinding.inflate(LayoutInflater.from(context))
        val addDialog = AlertDialog.Builder(context)
            .setView(setGroupNameDialogBinding.root)
            .create()

        with(setGroupNameDialogBinding) {
            this.dialogTitle.setText(title)
            if (title == "이름 변경") {
                this.dialogGroupName.setText(current_group_name)
            }
            this.btCancle.setOnClickListener {
                addDialog.dismiss()
            }
            this.btOk.setOnClickListener {
                if (this.dialogGroupName.text.isNullOrEmpty()) {
                    Toast.makeText(context, "빈 칸을 채워주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    when (title) {
                        "목록 추가" -> {
                            viewModel.addGroup(GroupEntity(group = this.dialogGroupName.text.toString()))
                        }
                        "이름 변경" -> {
                            current_group_name = this.dialogGroupName.text.toString()
                            viewModel.editGroup(
                                GroupEntity(
                                    id = current_group_id,
                                    group = current_group_name
                                )
                            )
                        }
                    }
                    addDialog.dismiss()
                }
            }
        }
        addDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addDialog.show()
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
                    itemInfoEditDialog("작업 수정", item_pos, context)
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
        if (title.equals("작업 수정")) {
            dialogBinding.etTitle.setText(viewModel.getToDoLiveDataInGroup().value!!.get(position).title)
            dialogBinding.etTimes.setText(viewModel.getToDoLiveDataInGroup().value!!.get(position).pomo.toString())
            dialogBinding.etStudy.setText(viewModel.getToDoLiveDataInGroup().value!!.get(position).study.toString())
            dialogBinding.etRest.setText(viewModel.getToDoLiveDataInGroup().value!!.get(position).short_rest.toString())
            dialogBinding.etLongRest.setText(viewModel.getToDoLiveDataInGroup().value!!.get(position).long_rest.toString())
            dialogBinding.cbPomoAutoRun.isChecked =
                viewModel.getToDoLiveDataInGroup().value!!.get(position).autoStart
            dialogBinding.cbNoLong.isChecked =
                viewModel.getToDoLiveDataInGroup().value!!.get(position).noLong
        } else {   //"작업 추가"
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
                            "작업 추가" -> {
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
                            "작업 수정" -> {
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
                group = current_group_id,
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
                group = current_group_id,
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