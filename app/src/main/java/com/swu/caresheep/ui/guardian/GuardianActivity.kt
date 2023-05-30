package com.swu.caresheep.ui.guardian

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.SensorEventListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivityGuardianBinding
import com.swu.caresheep.ui.guardian.calendar.GuardianCalendarFragment
import com.swu.caresheep.ui.guardian.home.GuardianHomeFragment
import com.swu.caresheep.ui.guardian.mypage.GuardianMyPageFragment
import pub.devrel.easypermissions.EasyPermissions
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.util.*

class GuardianActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks,SensorEventListener {

    private lateinit var binding: ActivityGuardianBinding

    var check : Int =0 // 한시간마다 노인 사용자가 움직였는지 체크하는 '횟수'

    lateinit var sensorManager: SensorManager
    var stepCountSensor: Sensor? = null

    // 현재 걸음 수
    var currentSteps = 0

    // 긴급 상황 확인
    var emergency : Int = 0

    @RequiresApi(api = Build.VERSION_CODES.Q)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initBottomNavigation()

        // Check activity recognition permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 0)
        }

        // Connect step sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        // Check if step sensor is available on the device
        if (stepCountSensor == null) {
            Toast.makeText(this, "No Step Sensor", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkMoving(){
        if(currentSteps>=1){
            // 긴급상황 아님
        }else if(currentSteps < 1){
            check++
            if(check >=24){
                // 긴급상황 발생
                emergency = 1;
                // 데이터 베이스에 긴급상황 관련 내용 추가 => 보호자측에서 볼 수 있게끔 하기
            }
        }
    }

    private fun pushEmergency(){

    }

    override fun onStart() {
        super.onStart()
        stepCountSensor?.let {
            // Set sensor speed
            sensorManager
            sensorManager!!.registerListener(
                this,
                stepCountSensor,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        // 걸음 센서 이벤트 발생시
        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values[0] == 1.0f) {
                // 센서 이벤트가 발생할때 마다 걸음수 증가
                currentSteps++
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

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