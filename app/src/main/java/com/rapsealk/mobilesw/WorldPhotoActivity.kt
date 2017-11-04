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
import android.os.AsyncTask
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.rapsealk.mobilesw.util.SharedPreferenceManager
import com.rapsealk.mobilesw.schema.Photo
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation

class WorldPhotoActivity : FragmentActivity(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener {

    private val FINE_LOCATION_CODE: Int = 1

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseAuthListener: FirebaseAuth.AuthStateListener? = null

    private var mSharedPreference: SharedPreferenceManager? = null

    // STATE FLAGS
    private var INITIAL_GPS_SET: Boolean = true
    private var DRAG_STATE: Boolean = false
    private var VIEW_PHOTOS_STATE: Boolean = false
    private var overlayState: Boolean = false

    private var overlays: ArrayList<GroundOverlay>? = null
    private var markers: ArrayList<Marker> = ArrayList<Marker>()

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
                        val ratio: Double = source.height.toDouble() / source.width.toDouble()
                        val targetHeight: Int = rootLinearLayout.height - 1000
                        val targetWidth: Int = (targetHeight * ratio).toInt()
                        val result: Bitmap = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false)
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
            val user: FirebaseUser? = auth.currentUser
            if (user != null) {
                // var intent = Intent(this, MainActivity::class.java)
                // startActivity(intent)
                // finish()
            } else {

            }
        }

        mSharedPreference = SharedPreferenceManager.getInstance(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment!!.getMapAsync(this)

        btnMagnify.setOnClickListener { view ->
            mMap!!.animateCamera(CameraUpdateFactory.zoomIn())
        }

        btnReduce.setOnClickListener { view ->
            mMap!!.animateCamera(CameraUpdateFactory.zoomOut())
        }

        btnState.setOnClickListener { view ->
            DRAG_STATE = !DRAG_STATE
            if (DRAG_STATE) btnState.text = "드래그!"
            else btnState.text = "영역 선택"
            mMap?.uiSettings!!.isScrollGesturesEnabled = !DRAG_STATE
            clearScreen()
        }

        btnOverlay.setOnClickListener { view ->
            toast("TODO : OutOfMemoryError")
            overlayState = overlayState.not()
            if (overlayState) {
                overlays = arrayListOf()
                db.getReference("photos").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot?) {
                        for (value in snapshot!!.children) {
                            val photo = value.getValue<Photo>(Photo::class.java)
                            GroundOverlayGenerator(this@WorldPhotoActivity, LatLng(photo.latitude, photo.longitude)).execute(photo.url)
                        }
                    }

                    override fun onCancelled(p0: DatabaseError?) { }
                })
            } else {
                overlays?.forEach { overlay -> overlay.remove() }
            }
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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap!!.uiSettings.isMapToolbarEnabled = false
        // mMap!!.uiSettings.isZoomControlsEnabled = true
        // mMap!!.uiSettings.isCompassEnabled = true

        mMap!!.setOnMapClickListener { point: LatLng ->
            draggableMarker?.remove()
            if (DRAG_STATE) {
                draggableMarker = mMap!!.addMarker(MarkerOptions()
                        .position(point)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                        .visible(true)
                        .draggable(true)
                        .apply { CustomDragMarkerListener().onMarkerDragStart(draggableMarker) })
                polygonStartPoint = point
            }
        }

        mMap!!.setOnMarkerDragListener(CustomDragMarkerListener())
        mMap!!.setOnCameraIdleListener(this)

        mMap!!.setOnPolygonClickListener { polygon: Polygon ->
            VIEW_PHOTOS_STATE = !VIEW_PHOTOS_STATE

            val params: ViewGroup.LayoutParams = mapFragment?.view!!.layoutParams
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
                horizontalScrollView?.y = 0f
                horizontalScrollView?.layoutParams?.width = LinearLayoutGPS.width
                horizontalScrollView?.layoutParams?.height = 400
                linearLayout = LinearLayout(this)
                linearLayout?.layoutParams?.width = horizontalScrollView?.width
                linearLayout?.layoutParams?.height = 400
                horizontalScrollView?.addView(linearLayout)
                rootLinearLayout.addView(horizontalScrollView)
            }
            mapFragment?.view!!.layoutParams = params

            val points: List<LatLng> = polygon.points
            // safety block
            val startPoint: LatLng = points.get(0)
            val endPoint: LatLng = points.get(2)

            // Check Photos : Query https://firebase.google.com/docs/database/android/lists-of-data?hl=ko
            ref = db.getReference("photos")

            val query: Query? = ref?.orderByChild("latitude")
                    ?.startAt(min(startPoint.latitude, endPoint.latitude))
                    ?.endAt(max(startPoint.latitude, endPoint.latitude))

            query?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot?) {

                    val ref2 = snapshot?.ref?.orderByChild("longitude")
                            ?.startAt(min(startPoint.longitude, endPoint.longitude))
                            ?.endAt(max(startPoint.longitude, endPoint.longitude))

                    ref2?.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot?) {

                            for (value in snapshot!!.children) {
                                val data = value.getValue<Photo>(Photo::class.java)
                                var url = data.url
                                if (url.equals("")) url = "https://firebasestorage.googleapis.com/v0/b/mobilesw-178816.appspot.com/o/ReactiveX.jpg?alt=media&token=510350fe-ac5b-4f01-9d9a-2fecf8428940"
                                val imageView = ImageView(this@WorldPhotoActivity)
                                /*
                                Glide.with(this@WorldPhotoActivity)
                                        .load(url)
                                        //.fitCenter()
                                        .override(480, 640)
                                        .into(imageView)
                                */
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

                                    val intent = Intent(applicationContext, PostActivity::class.java)
                                            .putExtra("SerializedData", data)
                                    startActivity(intent)
                                    this@WorldPhotoActivity.onPause()
                                }
                            }
                        }
                        override fun onCancelled(p0: DatabaseError?) { }
                    })
                }
                override fun onCancelled(p0: DatabaseError?) { }
            })
        }

        val lastKnownLocation = mSharedPreference?.getLastKnownLocation()
        if (lastKnownLocation != null) {
            // INITIAL_GPS_SET = false
            mMap!!.animateCamera(CameraUpdateFactory.zoomTo(100f))
            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(lastKnownLocation))
        }
    }

    // TODO : OnCameraIdleListener
    override fun onCameraIdle() {
        // var zoomLevel = mMap?.cameraPosition?.zoom
        // var imageSize = zoomLevel!! * 0.16f

        // if (overlayState) overlays?.forEach { overlay ->  }

        val bound = mMap!!.projection.visibleRegion.latLngBounds
        val _northeast = bound.northeast
        val _southwest = bound.southwest

        // Firebase Query
        ref = db.getReference("photos")

        val query: Query? = ref?.orderByChild("latitude")
                ?.startAt(min(_northeast.latitude, _southwest.latitude))
                ?.endAt(max(_northeast.latitude, _southwest.latitude))

        query?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot?) {

                val ref2 = snapshot?.ref?.orderByChild("longitude")
                        ?.startAt(min(_northeast.longitude, _southwest.longitude))
                        ?.endAt(max(_northeast.longitude, _southwest.longitude))

                ref2?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot?) {

                        for (value in snapshot!!.children) {
                            val data = value.getValue<Photo>(Photo::class.java)

                            val position = LatLng(data.latitude, data.longitude)
                            val marker = mMap?.addMarker(MarkerOptions().position(position).title(data.content))
                            markers.add(marker!!)
                            /*
                            var url = data.url
                            if (url.equals("")) url = "https://firebasestorage.googleapis.com/v0/b/mobilesw-178816.appspot.com/o/ReactiveX.jpg?alt=media&token=510350fe-ac5b-4f01-9d9a-2fecf8428940"
                            val imageView = ImageView(this@WorldPhotoActivity)
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

                                val intent = Intent(applicationContext, PostActivity::class.java)
                                        .putExtra("SerializedData", data)
                                startActivity(intent)
                                this@WorldPhotoActivity.onPause()
                            }
                            */
                        }
                    }
                    override fun onCancelled(p0: DatabaseError?) { }
                })
            }
            override fun onCancelled(p0: DatabaseError?) { }
        })

        Log.d("IDLE", "NORTHEAST: $_northeast, SOUTHWEST: $_southwest");
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
            val currentLatLng = LatLng(location.latitude, location.longitude)
            mSharedPreference?.setLastKnownLocation(currentLatLng)
            currentLocation?.latitude = location.latitude
            currentLocation?.longitude = location.longitude
            exMarker?.remove()
            exMarker = mMap?.addMarker(MarkerOptions().position(currentLatLng).title("#"+(++counter)))
            // mMap?.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
            tvLatitude.setText(location.latitude.toString())
            tvLongitude.setText(location.longitude.toString())
            tvAccuracy.setText(location.accuracy.toString())
            if (INITIAL_GPS_SET) {
                mMap?.animateCamera(CameraUpdateFactory.zoomBy(25f))
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
            val pointTopLeft = polygonStartPoint
            val pointBottomRight = marker!!.position
            val rectangle = PolygonOptions().add(
                    LatLng(pointTopLeft!!.latitude, pointTopLeft.longitude),
                    LatLng(pointTopLeft.latitude, pointBottomRight.longitude),
                    LatLng(pointBottomRight.latitude, pointBottomRight.longitude),
                    LatLng(pointBottomRight.latitude, pointTopLeft.longitude)
            ).strokeColor(Color.RED).fillColor(Color.YELLOW)
            exPolygon = mMap!!.addPolygon(rectangle)
            exPolygon!!.isClickable = true
        }

        override fun onMarkerDragEnd(marker: Marker?) {
            toast("onMarkerDragEnd")
        }
    }

    fun max(a: Double, b: Double): Double = if (a > b) a else b
    fun min(a: Double, b: Double): Double = if (a < b) a else b

    inner class GroundOverlayGenerator(val context: Context, val latlng: LatLng) : AsyncTask<String, Int, BitmapDescriptor>() {

        // private val context: Context
        // private val latlng: LatLng
        private var bitmapDescriptor: BitmapDescriptor? = null

        /*
        constructor(context: Context, latlng: LatLng) : super() {
            this.context = context
            this.latlng = latlng
        }
        */

        override fun doInBackground(vararg params: String?): BitmapDescriptor {
            val url = params.get(0)
            bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(Picasso.with(context).load(url).resize(160, 160).get())
            return bitmapDescriptor!!
        }

        override fun onPostExecute(result: BitmapDescriptor?) {
            overlays?.add(
                mMap?.addGroundOverlay(GroundOverlayOptions()
                        .image(bitmapDescriptor!!)
                        .position(latlng, 16f, 16f))!!
            )
        }
    }
}