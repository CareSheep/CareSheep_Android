package com.swu.caresheep.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.swu.caresheep.App.Companion.getApplicationContext
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.ElderMapsActivity
import com.swu.caresheep.ElderMapsActivity.Companion.moveAppSettings
import com.swu.caresheep.ui.start.user_id
import java.text.SimpleDateFormat
import java.util.*

const val FOREGROUND_LOCATION_PERMISSION_REQUEST_CODE = 100
const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 101

object LocationUtil {

    private var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallBack: LocationCallback  // GPS 응답 값 가져오기

    private lateinit var locationRequest: LocationRequest
    private const val priorityLocation: @Priority Int = Priority.PRIORITY_HIGH_ACCURACY
    private const val locationUpdateInterval: Long = 1000
    private var locationMarker: Marker? = null
    var mNaverMap: NaverMap? = null

    private lateinit var activity: ElderMapsActivity

    private val foregroundLocationPermissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @RequiresApi(Build.VERSION_CODES.Q)
    private const val backgroundLocationPermission = android.Manifest.permission.ACCESS_BACKGROUND_LOCATION

    init {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(getApplicationContext())
    }

    // 포그라운드 서비스 시작
    fun startGpsForegroundService() {
        val serviceIntent = Intent(getApplicationContext(), GpsForegroundService::class.java)
        ContextCompat.startForegroundService(getApplicationContext(), serviceIntent)
    }


    // 포그라운드 위치 권한 확인
    fun initForegroundLocationUtil(activity: Activity) {
        if (!hasForegroundLocationPermission(activity)) {
            ActivityCompat.requestPermissions(
                activity,
                foregroundLocationPermissions,
                FOREGROUND_LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        // 초기화
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(getApplicationContext())
    }

    // 백그라운드 위치 권한 확인
    fun initBackgroundLocationUtil(activity: ElderMapsActivity) {
        if (!hasBackgroundLocationPermission(activity)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(backgroundLocationPermission),
                    BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                // 설정으로 이동
                val builder = AlertDialog.Builder(activity)
                builder.setTitle("서비스 이용 알림").setCancelable(false)
                builder.setMessage("앱을 사용하기 위해서는 위치 권한이 필요합니다. 설정으로 이동하여 권한을 항상 허용해주세요.")
                builder.setPositiveButton("설정으로 이동") { _, _ ->
                    moveAppSettings(activity, BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE)
                }
                builder.show()
            }
        } else {
            NotificationManager.initNotificationManager(activity)
        }

        // 초기화
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
    }

    fun setMap(activity: ElderMapsActivity, mNaverMap: NaverMap) {
        this.activity = activity
        this.mNaverMap = mNaverMap
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        // 높은 정확도로 1초 간격 위치 업데이트 request 빌드
        locationRequest = LocationRequest.Builder(priorityLocation, locationUpdateInterval).apply {
            setMinUpdateIntervalMillis(locationUpdateInterval)
        }.build()

        locationCallBack = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                val currentLocation = p0.lastLocation
                mNaverMap?.let {
                    currentLocation?.let { currentLocation ->
                        setLastLocation(currentLocation, it)
                        saveLocationData(currentLocation)
                    }
                }

                Log.e("[GpsManager] currentLocation", currentLocation.toString())
            }
        }

        handleLocationPermissionChange()

        // 위치 업데이트 요청
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallBack,
            Looper.getMainLooper()
        )
    }

    // DB에 현재 위치 저장
    fun saveLocationData(currentLocation: Location) {
        val database = FirebaseDatabase.getInstance(DB_URL)
        val reference = database.getReference("Location")

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA).format(Date())

        val currentLocationData = com.swu.caresheep.Location(
            latitude = currentLocation.latitude.toString(),
            longitude = currentLocation.longitude.toString(),
            location_date = timeStamp,
            user_id = user_id
        )

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var existingDataKey: String? = null

                for (childSnapshot in dataSnapshot.children) {
                    val location = childSnapshot.getValue(com.swu.caresheep.Location::class.java)
                    if (location?.user_id == user_id) {
                        existingDataKey = childSnapshot.key
                        break
                    }
                }

                if (existingDataKey != null) {
                    // 이미 같은 user_id를 가진 데이터가 있는 경우
                    reference.child(existingDataKey).setValue(currentLocationData)
                        .addOnSuccessListener {
                            Log.d("[위치추적] - 어르신", "위치 DB 수정 성공")
                        }
                        .addOnFailureListener { exception ->
                            Log.e("[위치추적] - 어르신", "위치 데이터 수정 실패: ${exception.message}", exception)
                        }
                } else {
                    // 동일한 user_id를 가진 데이터가 없는 경우
                    val childCount = dataSnapshot.childrenCount

                    val id = (childCount + 1).toInt() // 데이터 count number

                    reference.child(id.toString()).setValue(currentLocationData)
                        .addOnSuccessListener {
                            Log.d("[위치추적] - 어르신", "위치 DB 추가 성공")
                        }
                        .addOnFailureListener { exception ->
                            Log.e("[위치추적] - 어르신", "위치 데이터 추가 실패: ${exception.message}", exception)
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("[위치추적] - 어르신", "Database error: $error")
            }
        })

    }

    // 내 위치를 마커로 출력
    fun setLastLocation(currentLocation: Location, mNaverMap: NaverMap) {
        if (mNaverMap.isDestroyed) {
            // naverMap 객체가 파괴되었다면 아무 작업도 수행하지 않음
            return
        }

        val myLocation = LatLng(currentLocation.latitude, currentLocation.longitude)

        // 이전에 생성한 마커가 있다면 제거
        locationMarker?.map = null

        // 새로운 마커 생성
        val marker = Marker()
        marker.position = myLocation
        marker.map = mNaverMap
        locationMarker = marker

        // 마커
        val cameraUpdate = CameraUpdate.scrollTo(myLocation)
        mNaverMap.moveCamera(cameraUpdate)
        mNaverMap.maxZoom = 18.0
        mNaverMap.minZoom = 5.0
    }

    fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack)
    }

    private fun handleLocationPermissionChange() {
        // 위치 권한 변경되었다면 포그라운드 서비스 종료 및 알림 제거
        if (!hasAllPermissions(getApplicationContext())) {
            getApplicationContext().stopService(
                Intent(
                    getApplicationContext(),
                    GpsForegroundService::class.java
                )
            )
        }
    }

    // 포그라운드 위치 권한 허용 여부 확인
    fun hasForegroundLocationPermission(context: Context): Boolean {
        for (permission in foregroundLocationPermissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    // 백그라운드 위치 권한 허용 여부 확인
    fun hasBackgroundLocationPermission(context: Context): Boolean {
        if (ContextCompat.checkSelfPermission(
                context,
                backgroundLocationPermission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return true
    }

    // 모든 위치 권한 허용 여부 확인
    fun hasAllPermissions(context: Context): Boolean {
        for (permission in foregroundLocationPermissions) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }

        return ActivityCompat.checkSelfPermission(
            context,
            backgroundLocationPermission
        ) == PackageManager.PERMISSION_GRANTED
    }

}