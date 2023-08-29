package com.swu.caresheep.ui.guardian.map

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.R
import com.swu.caresheep.data.model.Location
import com.swu.caresheep.databinding.ActivityGuardianMapsBinding
import com.swu.caresheep.ui.start.user_id
import com.swu.caresheep.utils.GoogleLoginClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GuardianMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityGuardianMapsBinding
    private val googleLoginClient = GoogleLoginClient()

    private var mNaverMap: NaverMap? = null
    private var locationMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startMapProcess()
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

    override fun onMapReady(naverMap: NaverMap) {
        mNaverMap = naverMap

        // DB에서 위치 정보 가져오기
        val database = FirebaseDatabase.getInstance(DB_URL)
        val reference = database.getReference("Location")
        val query = reference.orderByChild("user_id").equalTo(user_id.toDouble())

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // 위치 정보 조회
                val latestLocationSnapshot = dataSnapshot.children.lastOrNull()
                val locationData = latestLocationSnapshot?.getValue(Location::class.java)

                if (locationData != null) {  // 위치 정보가 있는 경우
                    val latitude = locationData.latitude.toDouble()
                    val longitude = locationData.longitude.toDouble()

                    if (locationMarker == null) {
                        // 처음에 마커 생성
                        val marker = Marker()
                        marker.position = LatLng(latitude, longitude)
                        marker.map = mNaverMap
                        locationMarker = marker
                    } else {
                        // 이미 생성된 마커 위치 업데이트
                        locationMarker?.position = LatLng(latitude, longitude)
                    }

                    // 카메라 이동
                    val cameraUpdate = CameraUpdate.scrollTo(LatLng(latitude, longitude))
                    naverMap.moveCamera(cameraUpdate)

                    // 정보창 생성
                    val infoWindow = InfoWindow()
                    infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(this@GuardianMapsActivity) {
                        override fun getText(infoWindow: InfoWindow): CharSequence {
                            return "어르신 위치"
                        }
                    }

                    // 지도를 클릭하면 정보 창을 닫음
                    naverMap.setOnMapClickListener { pointF, latLng ->
                        infoWindow.close()
                    }

                    // 마커를 클릭하면
                    val listener = Overlay.OnClickListener { overlay ->
                        val marker = overlay as Marker

                        if (marker.infoWindow == null) {
                            // 현재 마커에 정보 창이 열려있지 않을 경우 엶
                            infoWindow.open(marker)
                        } else {
                            // 이미 현재 마커에 정보 창이 열려있을 경우 닫음
                            infoWindow.close()
                        }

                        true
                    }

                    locationMarker?.onClickListener = listener

                    lifecycleScope.launch {
                        updateElderInfo()
                    }
                } else {  // 위치 정보가 없는 경우
                    Snackbar.make(binding.root, "어르신의 위치를 추적하는 중이 아닙니다.", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // 데이터 가져오기가 실패한 경우 처리할 내용
                Log.e("[위치추적] - 보호자", "Database error: $error")
            }
        })
    }

    // 어르신 정보 출력
    private suspend fun updateElderInfo() {
        val elderInfo = withContext(Dispatchers.IO) {
            googleLoginClient.getElderInfo(this@GuardianMapsActivity)
        }
        (elderInfo.username + " 어르신").also { binding.tvElderName.text = it }
        binding.tvElderInfo.text = elderInfo.gender

//        binding.tvElderAddress.text = ".............."
    }

}

