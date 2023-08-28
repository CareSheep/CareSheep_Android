package com.swu.caresheep.ui.elder.map

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.annotations.concurrent.UiThread
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivityElderMapsBinding
import com.swu.caresheep.utils.*

class ElderMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityElderMapsBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mNaverMap: NaverMap

    companion object {
        // 앱 설정 화면으로 이동
        fun moveAppSettings(elderMapsActivity: ElderMapsActivity, requestCode: Int) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", elderMapsActivity.packageName, null)
            intent.data = uri

            when (requestCode) {
                FOREGROUND_LOCATION_PERMISSION_REQUEST_CODE -> elderMapsActivity.moveLocationSettingsLauncher.launch(
                    intent
                )
                BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE -> elderMapsActivity.moveLocationSettingsLauncher.launch(
                    intent
                )
                NOTIFICATION_PERMISSION_REQUEST_CODE -> elderMapsActivity.moveNotificationSettingsLauncher.launch(
                    intent
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityElderMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)  // 자동으로 GPS 받아오기

        startMapProcess()

        // 백그라운드 위치 권한 요청
        LocationUtil.initBackgroundLocationUtil(this)
    }


    // 지도 연결 및 초기화
    private fun startMapProcess() {
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.fcv_map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.fcv_map, it).commit()
            }

        mapFragment.getMapAsync(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FOREGROUND_LOCATION_PERMISSION_REQUEST_CODE) {
            if (!LocationUtil.hasForegroundLocationPermission(this)) {
                LocationUtil.initForegroundLocationUtil(this)
            } else {
                NotificationManager.initNotificationManager(this)
            }
        } else if (requestCode == BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE) {
            if (LocationUtil.hasBackgroundLocationPermission(this)) {
                NotificationManager.initNotificationManager(this)
            } else {
                // 설정 이동
                val builder = AlertDialog.Builder(this)
                builder.setTitle("서비스 이용 알림").setCancelable(false)
                builder.setMessage("앱을 사용하기 위해서는 위치 권한이 필요합니다. 설정으로 이동하여 권한을 항상 허용해주세요.")
                builder.setPositiveButton("설정으로 이동") { _, _ ->
                    moveAppSettings(this, requestCode)
                }
                builder.show()
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (NotificationManager.hasNotificationPermission(this)) {
                // 권한이 허용된 경우
                if (LocationUtil.hasAllPermissions(this)) {
                    // 포그라운드 서비스 시작
                    val serviceIntent = Intent(this, GpsForegroundService::class.java)
                    ContextCompat.startForegroundService(this, serviceIntent)
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("서비스 이용 알림").setCancelable(false)
                    builder.setMessage("앱을 사용하기 위해서는 위치 권한이 필요합니다. 설정으로 이동하여 권한을 항상 허용해주세요.")
                    builder.setPositiveButton("설정으로 이동") { _, _ ->
                        moveAppSettings(this, FOREGROUND_LOCATION_PERMISSION_REQUEST_CODE)
                    }
                    builder.show()
                }
            } else {
                // 권한이 거부된 경우
                // 포그라운드 서비스가 진행 중이면 종료
                if (GpsForegroundService.isServiceRunning) {
                    val serviceIntent = Intent(this, GpsForegroundService::class.java)
                    stopService(serviceIntent)
                }
                if (LocationUtil.hasAllPermissions(this)) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("서비스 이용 알림").setCancelable(false)
                    builder.setMessage("앱을 사용하기 위해서는 알림 권한이 필요합니다. 설정으로 이동하여 권한을 허용해주세요.")
                    builder.setPositiveButton("설정으로 이동") { _, _ ->
                        moveAppSettings(this, requestCode)
                    }
                    builder.show()
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("서비스 이용 알림").setCancelable(false)
                    builder.setMessage("앱을 사용하기 위해서는 위치, 알림 권한이 필요합니다. 설정으로 이동하여 권한을 항상 허용해주세요.")
                    builder.setPositiveButton("설정으로 이동") { _, _ ->
                        moveAppSettings(this, requestCode)
                    }
                    builder.show()
                }
            }

        }
    }

    private val moveLocationSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // 설정 화면에서 돌아왔을 때의 처리
            LocationUtil.initBackgroundLocationUtil(this)

            if (LocationUtil.hasAllPermissions(this) && NotificationManager.hasNotificationPermission(
                    this
                ) && !GpsForegroundService.isServiceRunning
            ) {
                // 포그라운드 서비스 시작
                LocationUtil.startGpsForegroundService()
            }
        }

    private val moveNotificationSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // 설정 화면에서 돌아왔을 때의 처리
            NotificationManager.initNotificationManager(this)

            if (LocationUtil.hasAllPermissions(this) && NotificationManager.hasNotificationPermission(
                    this
                ) && !GpsForegroundService.isServiceRunning
            ) {
                // 포그라운드 서비스 재시작
                LocationUtil.startGpsForegroundService()
            }
        }


    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        this.mNaverMap = naverMap
        LocationUtil.setMap(this, mNaverMap)

    }

}