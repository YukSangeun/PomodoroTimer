package com.yukse.pomodorotimer

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.yukse.pomodorotimer.databinding.MainFragmentBinding

class MainFragment : Fragment() {

    private var _binding: MainFragmentBinding? = null
    private lateinit var sp: SharedPreferences


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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

        binding.btStart.setOnClickListener {
            val timer_intent = Intent(context, PomoTimerActivity::class.java)
            startActivity(timer_intent)
        }
    }

    //tool bar 에 menu 표시 설정 - add버튼 안보이도록
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.setGroupVisible(R.id.action_add_gorup, false)
    }

    override fun onStart() {
        super.onStart()
        binding.tvStudy.setText(sp.getInt("study_time", 25).toString())
        binding.tvRest.setText(sp.getInt("short_rest_time", 5).toString())
        if(sp.getBoolean("use_long_rest", true)) {
            binding.tvLongRest.setText(sp.getInt("long_rest_time", 30).toString())
            binding.tvPomo.setText(sp.getInt("long_rest_pomo", 4).toString())
        }
        else{
            binding.tvLongRest.setText("-")
            binding.tvPomo.setText("-")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}