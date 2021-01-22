package com.yukse.pomodorotimer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager

class MainActivity : AppCompatActivity() {
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
): FragmentStatePagerAdapter(fragmentManager){
    override fun getItem(position: Int): Fragment {
        when(position){
            0->{
                return TotalTimeFragment()
            }
            else->{
                return ToDoListFragment()
            }
        }
    }

    override fun getCount(): Int {
        return tabCount
    }
}