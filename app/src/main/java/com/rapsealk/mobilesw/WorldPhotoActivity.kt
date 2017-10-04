package com.rapsealk.mobilesw

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.support.v4.app.FragmentActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_world_photo.*

import android.location.Location
import android.location.LocationManager
import android.location.LocationListener
import android.support.v4.app.ActivityCompat
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.rapsealk.mobilesw.schema.Photo
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation

class WorldPhotoActivity : FragmentActivity(), OnMapReadyCallback {

    private val FINE_LOCATION_CODE: Int = 1

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseAuthListener: FirebaseAuth.AuthStateListener? = null

    private var mSharedPreference: SharedPreferenceManager? = null

    // STATE FLAGS
    private var INITIAL_GPS_SET: Boolean = true
    private var DRAG_STATE: Boolean = false
    private var VIEW_PHOTOS_STATE: Boolean = false

    private var mapFragment: SupportMapFragment? = null

    private var mMap: GoogleMap? = null
    private var mLocationManager: LocationManager? = null
    private var mLocationListener: LocationListener? = null
    private var draggableMarker: Marker? = null
    private var polygonStartPoint: LatLng? = null
    private var exPolygon: Polygon? = null

    // Runtime UI Component
    private var horizontalScrollView: HorizontalScrollView? = null
    private var linearLayout: LinearLayout? = null

    private var defaultPostImageLoadingView: ImageView? = null

    // Firebase Database
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var ref: DatabaseReference? = null

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_world_photo)

        defaultPostImageLoadingView = ImageView(this)
        Picasso.with(this@WorldPhotoActivity)
                .load(R.drawable.default_image)
                .transform(object : Transformation {
                    override fun key() : String = ""
                    override fun transform(source: Bitmap): Bitmap {
                        var ratio: Double = source.height.toDouble() / source.width.toDouble()
                        var targetHeight: Int = rootLinearLayout.height - 1000
                        var targetWidth: Int = (targetHeight * ratio).toInt()
                        var result: Bitmap = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false)
                        if (result != source) source.recycle()
                        return result
                    }
                }).into(defaultPostImageLoadingView)

        // Permission Check
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                toast("위치 정보를 이용하기 위해서는 권한이 필요합니다.")
            }
            ActivityCompat.requestPermissions(this, Array<String>(1) { Manifest.permission.ACCESS_FINE_LOCATION }, FINE_LOCATION_CODE)
        }

        // Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseAuthListener = FirebaseAuth.AuthStateListener() { auth: FirebaseAuth ->
            var user: FirebaseUser? = auth.currentUser
            if (user != null) {
                // var intent = Intent(this, MainActivity::class.java)
                // startActivity(intent)
                // finish()
            } else {

            }
        }

        mSharedPreference = SharedPreferenceManager.getInstance(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        /*val */mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment!!.getMapAsync(this)

        btnMagnify.setOnClickListener { view ->
            // toast("Magnifying!")
            mMap!!.animateCamera(CameraUpdateFactory.zoomIn())
        }

        btnReduce.setOnClickListener { view ->
            // toast("Reducing!")
            mMap!!.animateCamera(CameraUpdateFactory.zoomOut())
        }

        btnState.setOnClickListener { view ->
            DRAG_STATE = !DRAG_STATE
            if (DRAG_STATE) btnState.text = "드래그!"
            else btnState.text = "영역 선택"
            mMap?.uiSettings!!.isScrollGesturesEnabled = !DRAG_STATE
            clearScreen()
        }

        // GET GPS DATA
        try {
            mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            mLocationListener = CustomLocationListener()
            mLocationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1f, mLocationListener)
            mLocationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1f, mLocationListener)
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
        // mMap!!.uiSettings.isZoomControlsEnabled = true
        // mMap!!.uiSettings.isCompassEnabled = true

        // TODO : Customizing
        // https://developer.android.com/reference/android/view/GestureDetector.OnGestureListener.html
        // https://developer.android.com/reference/android/view/GestureDetector.OnGestureListener.html#onScroll(android.view.MotionEvent, android.view.MotionEvent, float, float)
        // UiSettings.setScrollGesturesEnabled(boolean)

        mMap!!.setOnMapClickListener { point: LatLng ->
            draggableMarker?.remove()
            if (DRAG_STATE) {
                draggableMarker = mMap!!.addMarker(MarkerOptions().position(point).visible(true).draggable(true).apply { CustomDragMarkerListener().onMarkerDragStart(draggableMarker) })
                polygonStartPoint = point
            }
        }

        mMap!!.setOnMarkerDragListener(CustomDragMarkerListener())

        mMap!!.setOnPolygonClickListener { polygon: Polygon ->
            VIEW_PHOTOS_STATE = !VIEW_PHOTOS_STATE

            var params: ViewGroup.LayoutParams = mapFragment?.view!!.layoutParams
            if (!VIEW_PHOTOS_STATE) {
                params.height += 400
                linearLayout?.removeAllViewsInLayout()
                horizontalScrollView?.removeAllViews()
                rootLinearLayout.removeView(horizontalScrollView)
            }
            else {
                params.height -= 400
                horizontalScrollView = HorizontalScrollView(this)
                horizontalScrollView?.x = LinearLayoutGPS.x
                horizontalScrollView?.y = 0f//LinearLayoutGPS.y// + LinearLayoutGPS.height
                horizontalScrollView?.layoutParams?.width = LinearLayoutGPS.width
                horizontalScrollView?.layoutParams?.height = 400
                linearLayout = LinearLayout(this)
                linearLayout?.layoutParams?.width = horizontalScrollView?.width
                linearLayout?.layoutParams?.height = 400
                horizontalScrollView?.addView(linearLayout)
                rootLinearLayout.addView(horizontalScrollView)
            }
            mapFragment?.view!!.layoutParams = params

            var points: List<LatLng> = polygon.points
            // safety block
            var startPoint: LatLng = points.get(0)
            var endPoint: LatLng = points.get(2)

            // Check Photos : Query https://firebase.google.com/docs/database/android/lists-of-data?hl=ko
            var user = mFirebaseAuth?.currentUser
            var uid = user?.uid

            ref = db.getReference("photos")

            var query: Query? = ref?.orderByChild("latitude")
                    ?.startAt(min(startPoint.latitude, endPoint.latitude))
                    ?.endAt(max(startPoint.latitude, endPoint.latitude))

            query?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot?) {

                    var ref2 = snapshot?.ref?.orderByChild("longitude")
                            ?.startAt(min(startPoint.longitude, endPoint.longitude))
                            ?.endAt(max(startPoint.longitude, endPoint.longitude))

                    ref2?.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot?) {

                            for (value in snapshot!!.children) {
                                var data = value.getValue<Photo>(Photo::class.java)
                                var url = data.url
                                if (url == null) url = "https://firebasestorage.googleapis.com/v0/b/mobilesw-178816.appspot.com/o/ReactiveX.jpg?alt=media&token=510350fe-ac5b-4f01-9d9a-2fecf8428940"
                                var imageView = ImageView(this@WorldPhotoActivity)
                                Picasso.with(this@WorldPhotoActivity)
                                        .load(url)
                                        .transform(object : Transformation {

                                            override fun key(): String = "resizeTransformation#" + System.currentTimeMillis()

                                            override fun transform(source: Bitmap): Bitmap {
                                                val ratio: Double = source.height.toDouble() / source.width.toDouble()
                                                val targetHeight: Int = 400
                                                val targetWidth: Int = (targetHeight * ratio).toInt()
                                                val result: Bitmap = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false)
                                                if (result != source) source.recycle()
                                                return result
                                            }
                                        })
                                        .into(imageView)
                                linearLayout?.addView(imageView)

                                imageView.setOnClickListener { view ->

                                    var intent = Intent(applicationContext, PostActivity::class.java)
                                            .putExtra("SerializedData", data)
                                    startActivity(intent)
                                    this@WorldPhotoActivity.onPause()
                                }
                            }
                        }

                        override fun onCancelled(p0: DatabaseError?) {
                            //
                        }
                    })
            }

                override fun onCancelled(p0: DatabaseError?) {
                    //
                }
            })
        }

        /* Add a marker in Seoul and move the camera
        val seoul = LatLng(37.56, 126.97)
        mMap!!.animateCamera(CameraUpdateFactory.zoomTo(10f))
        mMap!!.addMarker(MarkerOptions().position(seoul).title("Hi Seoul"))
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(seoul))
        */
        var lastKnownLocation = mSharedPreference?.getLastKnownLocation()
        if (lastKnownLocation != null) {
            mMap!!.animateCamera(CameraUpdateFactory.zoomTo(100f))
            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(lastKnownLocation))
        }
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

    private fun clearScreen(): Unit {
        draggableMarker?.remove()
        exPolygon?.remove()
        if (VIEW_PHOTOS_STATE) {
            VIEW_PHOTOS_STATE = !VIEW_PHOTOS_STATE
            mapFragment?.view!!.layoutParams.height += 400
            linearLayout?.removeAllViewsInLayout()
            horizontalScrollView?.removeAllViews()
            rootLinearLayout.removeView(horizontalScrollView)
        }
    }

    // CUSTOM INNER_CLASS IMPLEMENTS INTERFACE:LocationListener
    inner class CustomLocationListener : LocationListener {

        private var counter: Int = 0
        private var currentLocation: Location? = null
        private var exMarker: Marker? = null

        constructor() : super() {
            currentLocation = Location("user")
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        override fun onLocationChanged(location: Location) {
            var currentLatLng = LatLng(location.latitude, location.longitude)
            mSharedPreference?.setLastKnownLocation(currentLatLng)
            currentLocation?.latitude = location.latitude
            currentLocation?.longitude = location.longitude
            exMarker?.remove()
            exMarker = mMap!!.addMarker(MarkerOptions().position(currentLatLng).title("#"+(++counter)))
            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
            tvLatitude.setText(location.latitude.toString())
            tvLongitude.setText(location.longitude.toString())
            tvAccuracy.setText(location.accuracy.toString())
            if (INITIAL_GPS_SET) {
                mMap!!.animateCamera(CameraUpdateFactory.zoomBy(100f))
                INITIAL_GPS_SET = false
            }
        }

        override fun onProviderEnabled(provider: String?) {
            toast("ProviderEnabled: $provider")
        }

        override fun onProviderDisabled(provider: String?) {
            toast("ProviderDisabled: $provider")
        }
    }

    inner class CustomDragMarkerListener : GoogleMap.OnMarkerDragListener {

        constructor() : super() {}

        override fun onMarkerDragStart(marker: Marker?) {
            toast("onMarkerDragStart")
        }

        override fun onMarkerDrag(marker: Marker?) {
            exPolygon?.remove()
            var pointTopLeft = polygonStartPoint
            var pointBottomRight = marker!!.position
            var rectangle = PolygonOptions().add(
                    LatLng(pointTopLeft!!.latitude, pointTopLeft!!.longitude),
                    LatLng(pointTopLeft!!.latitude, pointBottomRight.longitude),
                    LatLng(pointBottomRight.latitude, pointBottomRight.longitude),
                    LatLng(pointBottomRight.latitude, pointTopLeft!!.longitude)
            ).strokeColor(Color.RED).fillColor(Color.YELLOW)
            exPolygon = mMap!!.addPolygon(rectangle)
            exPolygon!!.isClickable = true
        }

        override fun onMarkerDragEnd(marker: Marker?) {
            toast("onMarkerDragEnd")
        }
    }

    // image overray sample
    // https://developers.google.com/maps/documentation/android-api/groundoverlay?hl=ko

    fun max(a: Double, b: Double): Double = if (a > b) a else b
    fun min(a: Double, b: Double): Double = if (a < b) a else b

}