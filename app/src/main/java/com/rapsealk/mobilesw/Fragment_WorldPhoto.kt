package com.rapsealk.mobilesw

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.rapsealk.mobilesw.schema.Photo
import com.rapsealk.mobilesw.util.SharedPreferenceManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_world_photo.*
import kotlinx.android.synthetic.main.fragment_world_photo.*

class Fragment_WorldPhoto : Fragment(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener {

    private val FINE_LOCATION_CODE: Int = 1
    private val CHECK_POST_DELETED_CODE: Int = 11061

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseAuthListener: FirebaseAuth.AuthStateListener? = null

    private var mSharedPreference: SharedPreferenceManager? = null

    // STATE FLAGS
    private var INITIAL_GPS_SET: Boolean = true
    // private var DRAG_STATE: Boolean = false
    // private var VIEW_PHOTOS_STATE: Boolean = false
    private var OVERLAY_STATE: Boolean = false

    private var overlays: ArrayList<GroundOverlay>? = null
    private var markers: HashMap<String, Marker> = HashMap<String, Marker>()

    private var mGeocoder: Geocoder? = null

    private var mapFragment: SupportMapFragment? = null

    private var mGoogleMap: GoogleMap? = null
    private var mLocationManager: LocationManager? = null
    private var mLocationListener: LocationListener? = null
    // private var draggableMarker: Marker? = null
    // private var polygonStartPoint: LatLng? = null
    // private var exPolygon: Polygon? = null

    // Runtime UI Component
    // private var horizontalScrollView: HorizontalScrollView? = null
    // private var linearLayout: LinearLayout? = null

    // Firebase Database
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var ref: DatabaseReference? = null

    var ct: Context? = null;
    internal lateinit var mapView : MapView

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ct=container?.getContext()
        var view = inflater!!.inflate(R.layout.fragment_world_photo, container, false)

        mapView = view.findViewById(R.id.worldmap) as MapView/////////////////////////////

        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        try
        {
          MapsInitializer.initialize(getActivity().getApplicationContext());
        }
        catch (e:Exception)
        {
            e.printStackTrace();
        }
        //_map = mapView.getMapAsync()




        return view
    }

   // @SuppressLint("MissingPermission")
    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Permission Check

        if (ContextCompat.checkSelfPermission(ct!!, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
               // toast("위치 정보를 이용하기 위해서는 권한이 필요합니다.")
            }
            ActivityCompat.requestPermissions(activity, Array<String>(1) { android.Manifest.permission.ACCESS_FINE_LOCATION }, FINE_LOCATION_CODE)
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


        mSharedPreference = SharedPreferenceManager.getInstance(ct!!)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //mapFragment = fragmentManager.findFragmentById(R.id.worldmap) as MapFragment
        mapView!!.getMapAsync(this)

        mGeocoder = Geocoder(ct)

         btnMagnify.setOnClickListener { view -> mGoogleMap!!.animateCamera(CameraUpdateFactory.zoomIn()) }
         btnReduce.setOnClickListener { view -> mGoogleMap!!.animateCamera(CameraUpdateFactory.zoomOut()) }

        /*
        btnState.setOnClickListener { view ->
            DRAG_STATE = !DRAG_STATE
            if (DRAG_STATE) btnState.text = "드래그!"
            else btnState.text = "영역 선택"
            mGoogleMap?.uiSettings!!.isScrollGesturesEnabled = !DRAG_STATE
            clearScreen()
        }
        */

        btn_Overlay.setOnClickListener { view ->
         //   toast("TODO : OutOfMemoryError")
            OVERLAY_STATE = OVERLAY_STATE.not()
            if (OVERLAY_STATE) {
                overlays = arrayListOf()
                db.getReference("photos").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot?) {
                        for (value in snapshot!!.children) {
                            val photo = value.getValue<Photo>(Photo::class.java)
                            GroundOverlayGenerator(ct!!, LatLng(photo.latitude, photo.longitude)).execute(photo.url)
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
            mLocationManager = getActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            mLocationListener = CustomLocationListener()
            mLocationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1f, mLocationListener)
            mLocationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1f, mLocationListener)
        }
        catch (ex: Exception) {
           // toast(ex.toString())
            // finish()
        }


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        mGoogleMap!!.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? = null

            override fun getInfoContents(marker: Marker): View {
                val view: View = layoutInflater.inflate(R.layout.snippet_layout, null)
                // val snippets = marker.snippet.split("\n")
                if (marker.snippet != null) {
                    val url = marker.snippet.split("\n").get(1)
                    val imageView = view.findViewById(R.id.imageView) as ImageView
                    Picasso.with(ct).load(url).resize(192, 108).into(imageView)
                }
                val textView = view.findViewById(R.id.textView) as TextView
                textView.text = marker.title
                return view
            }
        })

        mGoogleMap!!.setOnInfoWindowClickListener { marker: Marker ->
            val snippets = marker.snippet.split("\n")
            val timestamp = snippets.get(0)
            db.getReference("photos/$timestamp").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.getValue<Photo>(Photo::class.java)
                    val intent = Intent(ct, PostActivity::class.java)
                            .putExtra("SerializedData", data)
                    startActivityForResult(intent, CHECK_POST_DELETED_CODE)
                   // ct.onPause()/////////////////////////////////////////////
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.d("Error", error.details)
                }
            })
        }

        mGoogleMap!!.uiSettings.isMapToolbarEnabled = false
        mGoogleMap!!.uiSettings.isZoomControlsEnabled = true
        mGoogleMap!!.uiSettings.isCompassEnabled = true

        /*
        mGoogleMap!!.setOnMapClickListener { point: LatLng ->
            draggableMarker?.remove()
            if (DRAG_STATE) {
                draggableMarker = mGoogleMap!!.addMarker(MarkerOptions()
                        .position(point)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                        .visible(true)
                        .draggable(true)
                        .apply { CustomDragMarkerListener().onMarkerDragStart(draggableMarker) })
                polygonStartPoint = point
            }
        }
        */

        // mGoogleMap!!.setOnMarkerDragListener(CustomDragMarkerListener())
        //mGoogleMap!!.setOnCameraIdleListener(ct)/////////////////////////////////////////////

        /*
        mGoogleMap!!.setOnPolygonClickListener { polygon: Polygon ->
            VIEW_PHOTOS_STATE = VIEW_PHOTOS_STATE.not()

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
                                //
                                //Glide.with(this@WorldPhotoActivity)
                                //        .load(url)
                                //        //.fitCenter()
                                //        .override(480, 640)
                                //        .into(imageView)
                                //
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
        */

        val lastKnownLocation = mSharedPreference?.getLastKnownLocation()
        if (lastKnownLocation != null) {
            // INITIAL_GPS_SET = false
            mGoogleMap!!.animateCamera(CameraUpdateFactory.zoomTo(25f))
            mGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLng(lastKnownLocation))
        }
    }

    override fun onCameraIdle() {
        // var zoomLevel = mGoogleMap?.cameraPosition?.zoom
        // var imageSize = zoomLevel!! * 0.16f

        // if (OVERLAY_STATE) overlays?.forEach { overlay ->  }

        val bound = mGoogleMap!!.projection.visibleRegion.latLngBounds
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

                            val timestamp = data.timestamp.toString()
                            if (markers.get(timestamp) != null) continue

                            val position = LatLng(data.latitude, data.longitude)
                            val marker = mGoogleMap?.addMarker(MarkerOptions().position(position).title(data.content).snippet("$timestamp\n"+data.url).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)))
                            // markers.add(marker!!)
                            markers.put(timestamp, marker!!)
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
        try {
            val address = mGeocoder?.getFromLocation((_northeast.latitude + _southwest.latitude) / 2f, (_northeast.longitude + _southwest.longitude) / 2f, 1)
            if (address!!.size > 0) tvAddress.text = address.get(0)?.getAddressLine(0) ?: address.get(0)?.adminArea
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }
/*
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            FINE_LOCATION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        //            toast("ACCESS_FINE_LOCATION PERMISSION GRANTED")
        //        else finish()
                return
            }
            else -> {
          //      finish()
            }
        }
    }
*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            CHECK_POST_DELETED_CODE -> {
                if (resultCode == FragmentActivity.RESULT_OK) {
                    val id = data.getStringExtra("id")
                    val marker = markers.remove(id)
                    if (marker != null) marker.remove()
                }
            }
        }
    }

    /*
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
    */

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
            exMarker = mGoogleMap?.addMarker(MarkerOptions().position(currentLatLng).title("#"+(++counter)))
            // mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
            // tvLatitude.text = location.latitude.toString()
            // tvLongitude.text = location.longitude.toString()
            // tvAccuracy.setText(location.accuracy.toString())
            // val address = mGeocoder?.getFromLocation(location.latitude, location.longitude, 1)
            // tvAddress.text = address?.get(0)?.adminArea
            if (INITIAL_GPS_SET) {
                mGoogleMap?.animateCamera(CameraUpdateFactory.zoomBy(25f))
                INITIAL_GPS_SET = false
            }
        }

        override fun onProviderEnabled(provider: String?) {
     //       toast("ProviderEnabled: $provider")
        }

        override fun onProviderDisabled(provider: String?) {
      //      toast("ProviderDisabled: $provider")
        }
    }

    /*
    inner class CustomDragMarkerListener : GoogleMap.OnMarkerDragListener {

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
            exPolygon = mGoogleMap!!.addPolygon(rectangle)
            exPolygon!!.isClickable = true
        }

        override fun onMarkerDragEnd(marker: Marker?) {
            toast("onMarkerDragEnd")
        }
    }
    */

    fun max(a: Double, b: Double): Double = if (a > b) a else b
    fun min(a: Double, b: Double): Double = if (a < b) a else b

    inner class GroundOverlayGenerator(val context: Context, val latlng: LatLng) : AsyncTask<String, Int, BitmapDescriptor>() {

        private var bitmapDescriptor: BitmapDescriptor? = null

        override fun doInBackground(vararg params: String?): BitmapDescriptor? {
            val url = params.get(0)
            // bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(Picasso.with(context).load(url).resize(160, 160).get())
            bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(Glide.with(context).load(url).asBitmap().thumbnail(0.3f).into(160, 160).get())
            return bitmapDescriptor!!
        }

        override fun onPostExecute(result: BitmapDescriptor?) {
            if (result == null) return
            overlays?.add(
                    mGoogleMap?.addGroundOverlay(GroundOverlayOptions()
                            .image(bitmapDescriptor!!)
                            .position(latlng, 16f, 16f))!!
            )
        }
    }
}



