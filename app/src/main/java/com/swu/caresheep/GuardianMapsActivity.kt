package com.swu.caresheep

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource

class GuardianMapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private val TAG = "MainActivity"

    // 권한
    private val PERMISSION_REQUEST_CODE = 100
    private val PERMISSIONS = arrayOf<String>(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private var mLocationSource: FusedLocationSource? = null
    private var mNaverMap: NaverMap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_maps)

        val fm: FragmentManager = supportFragmentManager
        var mapFragment: MapFragment? = fm.findFragmentById(R.id.guardian_map) as MapFragment?
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance()
            fm.beginTransaction().add(R.id.guardian_map, mapFragment).commit()
        }

        mapFragment!!.getMapAsync(this)

        mLocationSource = FusedLocationSource(this, PERMISSION_REQUEST_CODE)
    }

    override fun onMapReady(naverMap: NaverMap) {
        Log.d(TAG, "onMapReady")

        mNaverMap = naverMap

        // 파이어베이스에서 위치 정보 가져오기
        val database =
            FirebaseDatabase.getInstance("https://caresheep-dcb96-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val reference = database.getReference("Location")
        val query = reference.orderByChild("timestamp").limitToLast(1) //맨 위 하나

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // 가장 최근에 저장된 위치 정보 가져오기
                val latestLocationSnapshot = dataSnapshot.children.lastOrNull()
                val locationData = latestLocationSnapshot?.getValue(Location::class.java)

                if (locationData != null) {
                    // 위치 정보 화면에 표시
                    //카메라 이동
                    val latitude = locationData.latitude.toDouble()
                    val longitude = locationData.longitude.toDouble()
                    val cameraUpdate = CameraUpdate.scrollTo(LatLng(latitude, longitude))
                    naverMap.moveCamera(cameraUpdate)

                    // 마커 추가
                    val marker = Marker()
                    marker.position = LatLng(latitude, longitude)
                    marker.map = naverMap
                }

            }
            override fun onCancelled(databaseError: DatabaseError) {
                // 데이터 가져오기가 실패한 경우 처리할 내용
                val exception = databaseError.toException()
                Log.e("FirebaseDatabase", "Failed to read value: ${exception.message}", exception)
            }
        })
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mNaverMap!!.locationTrackingMode = LocationTrackingMode.Follow
            }
        }
    }
}

