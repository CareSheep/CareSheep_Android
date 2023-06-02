package com.swu.caresheep

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource
import com.swu.caresheep.BuildConfig.DB_URL
import java.text.SimpleDateFormat
import java.util.*


class ElderMapsActivity : AppCompatActivity(), OnMapReadyCallback {
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
        setContentView(R.layout.activity_elder_maps)


        val fm: FragmentManager = supportFragmentManager
        var mapFragment: MapFragment? = fm.findFragmentById(R.id.map) as MapFragment?
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance()
            fm.beginTransaction().add(R.id.map, mapFragment).commit()
        }

        mapFragment!!.getMapAsync(this)

        mLocationSource = FusedLocationSource(this, PERMISSION_REQUEST_CODE)
    }

    override fun onMapReady(naverMap: NaverMap) {
        Log.d(TAG, "onMapReady")

        mNaverMap = naverMap
        mNaverMap!!.locationSource = mLocationSource

        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE)



    }

    // 권한
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mNaverMap!!.locationTrackingMode = LocationTrackingMode.Follow

                // 위치 정보를 가져온 후 Firebase에 저장
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }

                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        // 위치 정보를 성공적으로 가져왔을 때
                        if (location != null) {
                            val database =
                                FirebaseDatabase.getInstance(DB_URL)
                                .getReference("Location")

                            // Voice의 각 필드에 넣기
                            val location = Location(
                                latitude = location.latitude.toString(),
                                longitude = location.longitude.toString(),
                                location_date = timeStamp,
                                user_id = 1,

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
                    }
                    .addOnFailureListener { exception ->
                        // 위치 정보를 가져오는 데 실패했을 때
                        Log.e(TAG, "Error getting location", exception)
                    }
            }
        }
    }
}