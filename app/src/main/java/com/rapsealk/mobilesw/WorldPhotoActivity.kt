package com.rapsealk.mobilesw

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.support.v4.app.FragmentActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_world_photo.*

class WorldPhotoActivity : FragmentActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private var mLocationManager: LocationManager? = getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private var mLocationListener: LocationListener? = CustomLocationListener();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_world_photo)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // GET GPS Data
        try {
            mLocationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1f, mLocationListener)
            mLocationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1f, mLocationListener)
        }
        catch (exception: Exception) {
            finish()
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

        // Add a marker in Sydney and move the camera
        // val sydney = LatLng(-34.0, 151.0)
        // mMap!!.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        // mMap!!.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    // CUSTOM CLASS EXTENDS "LocationListener"
    inner class CustomLocationListener : LocationListener {

        public var currentLocation: Location? = null

        constructor() : super() {
            currentLocation = Location("user")
            // location!!.longitude
            // location!!.latitude
        }

        override fun onLocationChanged(location: Location?) {
            currentLocation = location
            toast("Latitude: " + location!!.latitude + ", Longitude: " + location!!.longitude)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String?) {}
        override fun onProviderDisabled(provider: String?) {}
    }
}