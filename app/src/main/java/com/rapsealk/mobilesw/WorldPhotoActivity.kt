package com.rapsealk.mobilesw

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.FragmentActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_world_photo.*

import android.location.Location
import android.location.LocationManager
import android.location.LocationListener
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class WorldPhotoActivity : FragmentActivity(), OnMapReadyCallback {

    private val FINE_LOCATION_CODE: Int = 1

    private var mMap: GoogleMap? = null
    private var mLocationManager: LocationManager? = null
    private var mLocationListener: LocationListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_world_photo)

        // Permission Check
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                toast("위치 정보를 이용하기 위해서는 권한이 필요합니다.")
            }
            ActivityCompat.requestPermissions(this, Array<String>(1) { Manifest.permission.ACCESS_FINE_LOCATION }, FINE_LOCATION_CODE)
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnMagnify.setOnClickListener { view ->
            toast("Magnifying!")
            mMap!!.animateCamera(CameraUpdateFactory.zoomIn())
        }

        btnReduce.setOnClickListener { view ->
            toast("Reducing!")
            mMap!!.animateCamera(CameraUpdateFactory.zoomOut())
        }

        // GET GPS DATA
        try {
            mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            mLocationListener = CustomLocationListener()
            mLocationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1f, mLocationListener)
            mLocationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1f, mLocationListener)
        }
        catch (ex: Exception) {
            toast(ex.toString())
            // finish()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap!!.uiSettings.isMapToolbarEnabled = false

        mMap!!.setOnMapClickListener { point: LatLng ->
            toast("Click :: ("+point.latitude+", "+point.longitude+")")
        }

        // Add a marker in Sydney and move the camera
        val seoul = LatLng(37.56, 126.97)
        mMap!!.animateCamera(CameraUpdateFactory.zoomTo(10f))
        mMap!!.addMarker(MarkerOptions().position(seoul).title("Hi Seoul"))
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(seoul))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            FINE_LOCATION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    toast("ACCESS_FINE_LOCATION PERMISSION GRANTED")
                else finish()
                return
            }
            else -> {
                finish()
            }
        }
    }

    // CUSTOM INNER_CLASS IMPLEMENTS INTERFACE:LocationListener
    inner class CustomLocationListener : LocationListener {

        private var counter: Int = 0
        private var currentLocation: Location? = null

        constructor() : super() {
            currentLocation = Location("user")
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        override fun onLocationChanged(location: Location?) {
            var currentLatLng = LatLng(location!!.latitude, location!!.longitude)
            currentLocation?.latitude = location!!.latitude
            currentLocation?.longitude = location!!.longitude
            mMap!!.addMarker(MarkerOptions().position(currentLatLng).title("#"+(++counter)))
            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
            tvLatitude.setText(location!!.latitude.toString())
            tvLongitude.setText(location!!.longitude.toString())
            tvAccuracy.setText(location!!.accuracy.toString())
        }

        override fun onProviderEnabled(provider: String?) {
            toast("ProviderEnabled: $provider")
        }

        override fun onProviderDisabled(provider: String?) {
            toast("ProviderDisabled: $provider")
        }
    }

}