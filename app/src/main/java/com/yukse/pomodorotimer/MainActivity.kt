package com.yukse.pomodorotimer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager

class MainActivity : AppCompatActivity(), ToDoListFragment.OnDataPassLister {

    override fun onDataPass(
        study: Long?,
        shortRest: Long?,
        longRest: Long?,
        pomo: Int?,
        auto: Boolean?
    ) {
        val auto_ = if (auto == null) false else auto
        val studyT = if (study == null) 1 else study
        val shortRestT = if (shortRest == null) 1 else shortRest
        val longRestT = if (longRest == null) 1 else longRest
        val pomo_ = if (pomo == null) 4 else pomo

        Log.d("startt", "main auto: " + auto_)
        Log.d("startt", "study: " + studyT)
        Log.d("startt", "short: " + shortRestT)
        Log.d("startt", "longRest: " + longRestT)
        Log.d("startt", "pomo: " + pomo_)

        val intent: Intent = Intent(this@MainActivity, PomoTimerActivity::class.java)
        intent.putExtra("study", study)
        intent.putExtra("shortRest", shortRestT)
        intent.putExtra("longRest", longRestT)
        intent.putExtra("pomo", pomo_)
        intent.putExtra("auto", auto_)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //pager 만들기
        val pagerAdapter = FragmentPagerAdapter(supportFragmentManager, 2)
        findViewById<ViewPager>(R.id.vp_main).adapter = pagerAdapter
//        val view_pager = findViewById<ViewPager>(R.id.vp_main)


    }
}

class FragmentPagerAdapter(
    fragmentManager: FragmentManager,
    val tabCount: Int
) : FragmentStatePagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                return TotalTimeFragment()
            }
            else -> {
                return ToDoListFragment()
            }
        }
    }

    override fun getCount(): Int {
        return tabCount
    }
}