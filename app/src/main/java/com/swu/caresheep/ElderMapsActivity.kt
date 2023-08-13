package com.swu.caresheep

import android.Manifest
import android.location.Location
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.annotations.concurrent.UiThread
import com.google.firebase.database.FirebaseDatabase
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.swu.caresheep.BuildConfig.DB_URL
import java.text.SimpleDateFormat
import java.util.*


class ElderMapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private val TAG = "MainActivity"

    // 권한
    private val PERMISSION_REQUEST_CODE = 100
//    private val PERMISSIONS = arrayOf<String>(
//        Manifest.permission.ACCESS_FINE_LOCATION,
//        Manifest.permission.ACCESS_COARSE_LOCATION
//    )
    var permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION)
    private var mLocationSource: FusedLocationSource? = null
    private var mNaverMap: NaverMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_elder_maps)


//        val fm: FragmentManager = supportFragmentManager
//        var mapFragment: MapFragment? = fm.findFragmentById(R.id.map) as MapFragment?
//        if (mapFragment == null) {
//            mapFragment = MapFragment.newInstance()
//            fm.beginTransaction().add(R.id.map, mapFragment).commit()
//        }
//        // callback으로 onMapReady에 연결
//        mapFragment!!.getMapAsync(this)
//
//        mLocationSource = FusedLocationSource(this, PERMISSION_REQUEST_CODE)
//

        if(isPermitted())
            startProcess()
        else
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)

    }

    // 권한 확인
    fun isPermitted() : Boolean {
        for(perm in permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    // 권한이 있을 때 callback으로 onMapReady에 연결해서 startProcess 함수 실행
    fun startProcess() {
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }
        mapFragment.getMapAsync(this)
    }

    @UiThread
    // GPS 가져오기
    override fun onMapReady(naverMap: NaverMap) {
        Log.d(TAG, "onMapReady")

//        mNaverMap = naverMap
//        mNaverMap!!.locationSource = mLocationSource
//
//        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE)

        val cameraPosition = CameraPosition(
            LatLng(37.5666102, 126.9783881), // 위치 지정(현재 GPS로 이동할 것이라 임의로)
            16.0 // 줌 레벨
            )
        naverMap.cameraPosition = cameraPosition
        this.mNaverMap = naverMap

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this) // 자동으로 GPS 받아오기
        setUpdateLocationListener() // 내 위치 가져오기
    }
    lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    lateinit var locationCallBack : LocationCallback // GPS 응답 값 가져오기


    // 좌표계 주기적 갱신
    @SuppressLint("MissingPermission")
    fun setUpdateLocationListener() {
        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // 높은 정확도
            interval = 1000 // 1초에 한번씩 GPS 요청
        }
        locationCallBack = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?: return
                for ((i, location) in locationResult.locations.withIndex()) {
                    Log.d("location: ", "${location.latitude}, ${location.longitude}")
                    setLastLocation(location)
                }
            }
        }
        //location 요청 함수 호출 (locationRequest, locationCallback)
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallBack,
            Looper.myLooper()
        )
    }

    //setUpdateLocationListner 함수에서 받아온 GPS 좌표로 현재 위치를 마커로 출력
    fun setLastLocation(location: Location) {
        val myLocation = LatLng(location.latitude, location.longitude)
        val marker = Marker()
        marker.position = myLocation

        marker.map = mNaverMap
        //마커
        val cameraUpdate = CameraUpdate.scrollTo(myLocation)
        mNaverMap?.moveCamera(cameraUpdate)
        mNaverMap?.maxZoom = 18.0
        mNaverMap?.minZoom = 5.0

        // 파이어베이스 DB에 현재 위치 저장
        val database = FirebaseDatabase.getInstance(DB_URL).getReference("Location")

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        // Location의 각 필드에 넣기
        val locationData = Location(
            latitude = location.latitude.toString(),
            longitude  = location.longitude.toString(),
            location_date = timeStamp,
            user_id = 1
        )
        database.child(timeStamp).setValue(location)   // 데이터가 계속 쌓이도록(timeStamp가 참조 꼬리로 쌓이도록)
            //업로드 성공했는지 확인해보려고
            .addOnSuccessListener {
                Log.d("Firebase", "데이터 업로드 성공")
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "데이터 업로드 실패: ${exception.message}", exception)
            }
    }



//    // 권한
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if (requestCode == PERMISSION_REQUEST_CODE) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                mNaverMap!!.locationTrackingMode = LocationTrackingMode.Follow
//
//                // 위치 정보를 가져온 후 Firebase에 저장
//                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//                if (ActivityCompat.checkSelfPermission(
//                        this,
//                        Manifest.permission.ACCESS_FINE_LOCATION
//                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                        this,
//                        Manifest.permission.ACCESS_COARSE_LOCATION
//                    ) != PackageManager.PERMISSION_GRANTED
//                ) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    return
//                }
//
//                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//
//                fusedLocationClient.lastLocation
//                    .addOnSuccessListener { location ->
//                        // 위치 정보를 성공적으로 가져왔을 때
//                        if (location != null) {
//                            val database =
//                                FirebaseDatabase.getInstance(DB_URL)
//                                    .getReference("Location")
//
//                            // Voice의 각 필드에 넣기
//                            val location = Location(
//                                latitude = location.latitude.toString(),
//                                longitude = location.longitude.toString(),
//                                location_date = timeStamp,
//                                user_id = 1,
//
//                                )
//                            database.child(timeStamp).setValue(location)   // 데이터가 계속 쌓이도록(timeStamp가 참조 꼬리로 쌓이도록)
//                                //업로드 성공했는지 확인해보려고
//                                .addOnSuccessListener {
//                                    Log.d("Firebase", "데이터 업로드 성공")
//                                }
//                                .addOnFailureListener { exception ->
//                                    Log.e("Firebase", "데이터 업로드 실패: ${exception.message}", exception)
//                                }
//                        }
//                    }
//                    .addOnFailureListener { exception ->
//                        // 위치 정보를 가져오는 데 실패했을 때
//                        Log.e(TAG, "Error getting location", exception)
//                    }
//            }
//        }
//    }


}