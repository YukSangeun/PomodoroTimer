package com.yukse.pomodorotimer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class TotalTimeFragment : Fragment(){

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
        return inflater.inflate(R.layout.totaltime_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.bt_start).setOnClickListener {
            val timer_intent = Intent(context, PomoTimerActivity::class.java)
            startActivity(timer_intent)
        }
    }
}