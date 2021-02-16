package com.yukse.pomodorotimer

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.yukse.pomodorotimer.databinding.MainFragmentBinding
import com.yukse.pomodorotimer.databinding.TodoItemActionDialogBinding

class MainFragment : Fragment() {

    private var _binding: MainFragmentBinding? = null
    private lateinit var sp: SharedPreferences


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var pomoSetString: Array<String>
    private val pomoSet = arrayOf(arrayOf(25, 5, 4, 20), arrayOf(50, 10, 4, 40), arrayOf(75,5, 4, 40))

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
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sp = PreferenceManager.getDefaultSharedPreferences(context)

        pomoSetString = resources.getStringArray(R.array.pomodoro_set)

        binding.btStart.setOnClickListener {
            val timer_intent = Intent(context, PomoTimerActivity::class.java)
            startActivity(timer_intent)
        }
        binding.llTimeBox.setOnClickListener {
            selectPomoSet()
        }
    }

    //tool bar 에 menu 표시 설정 - add버튼 안보이도록
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.setGroupVisible(R.id.action_todo_gorup, false)
    }

    override fun onStart() {
        super.onStart()
        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun init(){
        binding.tvStudy.setText(sp.getInt("study_time", 25).toString())
        binding.tvRest.setText(sp.getInt("short_rest_time", 5).toString())
        if (sp.getBoolean("use_long_rest", true)) {
            binding.tvLongRest.setText(sp.getInt("long_rest_time", 20).toString())
            binding.tvPomo.setText(sp.getInt("long_rest_pomo", 4).toString())
        } else {
            binding.tvLongRest.setText("-")
            binding.tvPomo.setText("-")
        }
    }

    fun selectPomoSet(){
        val dialogBinding = TodoItemActionDialogBinding.inflate(LayoutInflater.from(context))
        dialogBinding.dialogTitle.setText(R.string.pomodoro_set_dialog_title)
        dialogBinding.lvSelectAction.adapter =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, pomoSetString)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.lvSelectAction.setOnItemClickListener { parent, view, position, id ->
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putInt("study_time", pomoSet[position][0])
            editor.putInt("short_rest_time", pomoSet[position][1])
            editor.putInt("long_rest_pomo", pomoSet[position][2])
            editor.putInt("long_rest_time", pomoSet[position][3])
            editor.putBoolean("use_long_rest", true)
            editor.commit()
            dialog.dismiss()
            init()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }
}