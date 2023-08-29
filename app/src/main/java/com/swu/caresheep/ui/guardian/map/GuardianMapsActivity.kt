package com.swu.caresheep.ui.guardian.map

import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

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

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_in_right, R.anim.none)

        // 뒤로 가기
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.none, R.anim.slide_out_right)
    }

    /**
     * 지도 연결 및 초기화
     */
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

                    // 애니메이션 설정
                    val fadeInAnimation =
                        AnimationUtils.loadAnimation(this@GuardianMapsActivity, R.anim.fade_in)
                    fadeInAnimation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {
                            binding.clElder.visibility = View.VISIBLE
                        }

                        override fun onAnimationEnd(animation: Animation?) {}

                        override fun onAnimationRepeat(animation: Animation?) {}
                    })

                    val fadeOutAnimation =
                        AnimationUtils.loadAnimation(this@GuardianMapsActivity, R.anim.fade_out)
                    fadeOutAnimation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {}

                        override fun onAnimationEnd(animation: Animation?) {
                            binding.clElder.visibility = View.GONE
                        }

                        override fun onAnimationRepeat(animation: Animation?) {}
                    })

                    // 정보창 생성
                    val infoWindow = createInfoWindow()

                    // 지도를 클릭하면 정보 창을 닫음
                    naverMap.setOnMapClickListener { _, _ ->
                        infoWindow.close()
                        binding.clElder.startAnimation(fadeOutAnimation)
                    }

                    // 마커를 클릭하면
                    val listener = Overlay.OnClickListener { overlay ->
                        val marker = overlay as Marker

                        if (marker.infoWindow == null) {
                            // 현재 마커에 정보 창이 열려있지 않을 경우 엶
                            infoWindow.open(marker)
                            binding.clElder.startAnimation(fadeInAnimation)
                        } else {
                            // 이미 현재 마커에 정보 창이 열려있을 경우 닫음
                            infoWindow.close()
                            binding.clElder.startAnimation(fadeOutAnimation)
                        }

                        true
                    }

                    locationMarker?.onClickListener = listener

                    // 어르신 정보 업데이트
                    lifecycleScope.launch {
                        updateElderInfo(latitude, longitude)
                    }
                } else {  // 위치 정보가 없는 경우
                    Snackbar.make(binding.root, "어르신의 위치를 추적하는 중이 아닙니다.", Snackbar.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // 데이터 가져오기가 실패한 경우 처리할 내용
                Log.e("[위치추적] - 보호자", "Database error: $error")
            }
        })
    }

    /**
     * 어르신 정보 출력
     */
    private suspend fun updateElderInfo(latitude: Double, longitude: Double) {
        val elderInfo = withContext(Dispatchers.IO) {
            googleLoginClient.getElderInfo(this@GuardianMapsActivity)
        }
        (elderInfo.username + " 어르신").also { binding.tvElderName.text = it }
        (elderInfo.age.toString() + "세/" + elderInfo.gender).also { binding.tvElderInfo.text = it }
        binding.tvElderAddress.text = getAddress(latitude, longitude)
    }

    /**
     * 정보창 생성
     */
    private fun createInfoWindow(): InfoWindow {
        val infoWindow = InfoWindow()
        infoWindow.alpha = 0.85f
        infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(this@GuardianMapsActivity) {
            override fun getText(infoWindow: InfoWindow): CharSequence {
                return "어르신 위치"
            }
        }
        return infoWindow
    }

    /**
     * 좌표를 주소로 변환
     * @param latitude 위도
     * @param longitude 경도
     * @return 변환된 주소 문자열
     */
    private fun getAddress(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.KOREA)
        var addressList = ArrayList<Address>()
        val maxResults = 1
        var addressResult = getString(R.string.guardian_maps_address_not_available)
        try {
            addressList = if (Build.VERSION.SDK_INT >= 33) {
                val geocodeListener = Geocoder.GeocodeListener { addresses ->
                    addressList = addresses as ArrayList<Address>
                }
                geocoder.getFromLocation(latitude, longitude, maxResults, geocodeListener)
                addressList
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, maxResults) as ArrayList<Address>
            }

            if (addressList.isNotEmpty()) {
                // 주소 받아오기
                val currentLocationAddress = addressList[0].getAddressLine(0).toString()
                addressResult = currentLocationAddress
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addressResult
    }

}