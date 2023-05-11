package com.swu.caresheep.ui.guardian

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivityGuardianElderReportBinding

class GuardianElderReportActivity : AppCompatActivity() {

    lateinit var binding : ActivityGuardianElderReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianElderReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pager: ViewPager2 = binding.fragmentContainer
        val tab: TabLayout = binding.contentsTabLayout

        pager.adapter = GuardianEldereReportViewAdapter(supportFragmentManager, lifecycle)

        TabLayoutMediator(tab, pager){ tab, position ->
            when(position){
                0->{
                    tab.text = "일간 리포트"
                }
                1->{
                    tab.text = "주간 리포트"
                }
            }
        }.attach()

        //supportFragmentManager.beginTransaction().add(R.id.fragmentContainer,GuardianElderTodayReportFragment()).commit()

//        binding.goToReport.setOnClickListener {
//            //replaceFragment(GuardianElderTodayReportFragment())
//            supportFragmentManager.beginTransaction().add(R.id.fragmentContainer,GuardianElderTodayReportFragment()).commit()
//        }

    }



    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer,fragment)
        fragmentTransaction.commit()
    }
}