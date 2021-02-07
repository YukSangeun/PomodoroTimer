package com.yukse.pomodorotimer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.yukse.pomodorotimer.database.ToDoEntity

class MainActivity : AppCompatActivity(), ToDoListFragment.OnDataPassLister {

    private var backBtnTime: Long = 0

    //to_do_list_fragment에서 전달받은 데이더 -> timer activity로 전달 (중간자)
    override fun onDataPass(item: ToDoEntity) {
        val sp = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)

        val studyT = if (item.study == null) sp.getInt("study_time", 1).toLong() else item.study
        val shortRestT = if (item.short_rest == null) sp.getInt("short_rest_time", 1)
            .toLong() else item.short_rest
        val longRestT =
            if (item.long_rest == null) sp.getInt("long_rest_time", 1).toLong() else item.long_rest
        val pomo = if (item.pomo == null) sp.getInt("long_rest_pomo", 4) else item.pomo
        val auto =
            if (item.autoStart == null) sp.getBoolean("auto_timer", false) else item.autoStart
        val noLongRest =
            if (item.noLong == null) !(sp.getBoolean("use_long_rest", true)) else item.noLong

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

        //view pager2
        val pagerAdapter = FragmentPagerAdapter(this, 2)
        findViewById<ViewPager2>(R.id.vp_main).adapter = pagerAdapter
        // view pager setting
        with(findViewById<ViewPager2>(R.id.vp_main)) {
            this.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            this.currentItem = 0
        }

    }

    override fun onBackPressed() {
        val curTime: Long = System.currentTimeMillis();
        val gapTime: Long = curTime - backBtnTime;

        if (0 <= gapTime && 2000 >= gapTime) {
            super.onBackPressed();
        } else {
            backBtnTime = curTime;
            Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }
}

class FragmentPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val tabCount: Int
) : FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> {
                return TotalTimeFragment()
            }
            else -> {
                return ToDoListFragment()
            }
        }
    }

    override fun getItemCount(): Int {
        return tabCount
    }
}