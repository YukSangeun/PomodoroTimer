package com.yukse.pomodorotimer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.yukse.pomodorotimer.databinding.TotaltimeFragmentBinding

class TotalTimeFragment : Fragment(){

    private var _binding: TotaltimeFragmentBinding? = null
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

        //프래그먼트가 인터페이스를 처음으로 그릴 때 호출된다.
        _binding = TotaltimeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btStart.setOnClickListener {
            val timer_intent = Intent(context, PomoTimerActivity::class.java)
            startActivity(timer_intent)
        }

        binding.btSetting.setOnClickListener{
            val setting_intnet = Intent(context, SettingActivity::class.java)
            startActivity(setting_intnet)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}