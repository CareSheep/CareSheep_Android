package com.swu.caresheep.guardian.report

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivityGuardianElderReportBinding
import com.swu.caresheep.guardian.GuardianActivity
import kotlinx.android.synthetic.main.activity_guardian_elder_report.go_to_back

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

        // 보호자 처음 화면으로 돌아감
        go_to_back.setOnClickListener {
            finish()
        }

    }



    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer,fragment)
        fragmentTransaction.commit()
    }
}