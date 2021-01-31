package com.yukse.pomodorotimer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager

class MainActivity : AppCompatActivity(), ToDoListFragment.OnDataPassLister {
    //to_do_list_fragment에서 전달받은 데이더 -> timer activity로 전달 (중간자)
    override fun onDataPass(item: Todo) {
        val sp = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)

        val studyT = if (item.studyTime == null) sp.getInt("study_time",1).toLong() else item.studyTime
        val shortRestT = if (item.shortRestTime == null) sp.getInt("short_rest_time",1).toLong() else item.shortRestTime
        val longRestT = if (item.longRestTime == null) sp.getInt("long_rest_time", 1).toLong()else item.longRestTime
        val pomo = if (item.pomoCnt == null) sp.getInt("long_rest_pomo", 4) else item.pomoCnt
        val auto = if (item.autoStart == null) sp.getBoolean("auto_timer", false) else item.autoStart
        val noLongRest = if(item.noLongRest == null) !(sp.getBoolean("use_long_rest", true)) else item.noLongRest

        val intent: Intent = Intent(this@MainActivity, PomoTimerActivity::class.java)
        intent.putExtra("study", studyT)
        intent.putExtra("shortRest", shortRestT)
        intent.putExtra("longRest", longRestT)
        intent.putExtra("pomo", pomo)
        intent.putExtra("auto", auto)
        intent.putExtra("noLongRest", noLongRest)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //pager 만들기
        val pagerAdapter = FragmentPagerAdapter(supportFragmentManager, 2)
        findViewById<ViewPager>(R.id.vp_main).adapter = pagerAdapter
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