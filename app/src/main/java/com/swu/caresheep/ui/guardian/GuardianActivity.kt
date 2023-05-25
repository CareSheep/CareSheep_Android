package com.swu.caresheep.ui.guardian

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivityGuardianBinding
import com.swu.caresheep.ui.guardian.calendar.GuardianCalendarFragment
import com.swu.caresheep.ui.guardian.home.GuardianHomeFragment
import com.swu.caresheep.ui.guardian.mypage.GuardianMyPageFragment
import pub.devrel.easypermissions.EasyPermissions

class GuardianActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private lateinit var binding: ActivityGuardianBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initBottomNavigation()
    }

    private fun initBottomNavigation() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frm_main, GuardianHomeFragment())
            .commitAllowingStateLoss()
        binding.bnvGuardian.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frm_main, GuardianHomeFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
                R.id.calendarFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frm_main, GuardianCalendarFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
                R.id.myPageFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frm_main, GuardianMyPageFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }

    /**
     * EasyPermissions 라이브러리를 사용하여 요청한 권한을 사용자가 승인한 경우 호출된다
     */
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    /**
     * EasyPermissions 라이브러리를 사용하여 요청한 권한을 사용자가 거부한 경우 호출된다
     */
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
    }
}