package com.swu.caresheep

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private val TAG = "MainActivity"

    private val PERMISSION_REQUEST_CODE = 100
    private val PERMISSIONS = arrayOf<String>(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private var mLocationSource: FusedLocationSource? = null
    private var mNaverMap: NaverMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)


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