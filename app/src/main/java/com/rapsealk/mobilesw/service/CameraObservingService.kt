package com.rapsealk.mobilesw.service

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.hardware.Camera
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.provider.MediaStore
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.rapsealk.mobilesw.R
import com.rapsealk.mobilesw.UploadPhotoActivity
import com.rapsealk.mobilesw.util.SharedPreferenceManager

/**
 * Created by rapsealk on 2017. 9. 24..
 */
class CameraObservingService : Service, LocationListener {

    private var cameraOnUse = false
    private var isStop = false

    private var mLocationManager: LocationManager? = null
    private var mSharedPreference: SharedPreferenceManager? = null

    var t : Long=0

    constructor() : super() {}

    override fun onCreate() {
        super.onCreate()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            isStop = true
            Toast.makeText(applicationContext, "CameraService는 위치 정보에 대한 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        mSharedPreference = SharedPreferenceManager.getInstance(applicationContext)

        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mLocationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 1f, this)
        mLocationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 1f, this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val observer = Thread(CameraObserver())
        observer.start()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        isStop = true
        mLocationManager?.removeUpdates(this)
    }

    fun onCameraUse(): Boolean {
        var camera: Camera? = null

        try {
            camera = Camera.open()
        }
        catch (e: RuntimeException) {
            return true
        }
        finally {
            if (camera != null) camera.release()
        }
        return false
    }

    private inner class CameraObserver : Runnable {

        private val handler: Handler = Handler()

        override fun run() {
            while (!isStop) {
                if (!onCameraUse() && !cameraOnUse) { }
                else if (onCameraUse() && !cameraOnUse) {
                    cameraOnUse = true
                    t = System.currentTimeMillis()
                }
                else if (onCameraUse() && cameraOnUse) { }
                else if (!onCameraUse() && cameraOnUse) {

                    cameraOnUse = false

                    val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA)
                    val imageCursor : Cursor? = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                            MediaStore.Images.Media.DATE_TAKEN + " >=" + t, null, MediaStore.Images.Media.DATE_ADDED + " desc ")
                    if (imageCursor == null || imageCursor.count==0){
                        continue
                    }

                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val uploadImageIntent = Intent(this@CameraObservingService, UploadPhotoActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

                    val uploadImagePendingIntent = PendingIntent.getActivity(this@CameraObservingService, 1, uploadImageIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    val select = Intent(applicationContext,AlarmReceiver::class.java)
                    select.setAction("YES_ACTION")
                    select.putExtra("time",t)
                    val selectPendingIntent = PendingIntent.getBroadcast(applicationContext, 0, select, PendingIntent.FLAG_UPDATE_CURRENT)

                    val cancel = Intent(applicationContext,AlarmReceiver::class.java)
                    cancel.setAction("NO_ACTION")
                    val cancelPendingIntent = PendingIntent.getBroadcast(applicationContext, 123456, cancel, PendingIntent.FLAG_UPDATE_CURRENT)


                    val contentTitleList: Array<String> = arrayOf(
                            "이곳에서 찍은 사진이 마음에 드시나요?",
                            "다른 사람들에게 소개하고 싶은 곳인가요?",
                            "추억을 공유하고 싶으신가요?"
                    )

                    val titleIndex = ((System.currentTimeMillis().toInt() % contentTitleList.size.toInt()) + contentTitleList.size.toInt()) % contentTitleList.size.toInt()

                    val ncbuilder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext)
                            .setFullScreenIntent(uploadImagePendingIntent, true)
                            .setContentIntent(uploadImagePendingIntent)
                            .setSmallIcon(R.drawable.ic_instagram)
                            .setContentTitle(contentTitleList.get(titleIndex))
                            .setContentText("지금 포플에 업로드하세요!")
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setAutoCancel(true)
                            .addAction(android.R.drawable.star_on, "올리기", selectPendingIntent)
                            .addAction(android.R.drawable.star_off, "닫기", cancelPendingIntent)


                    notificationManager.notify(9999, ncbuilder.build())
                   // break
                }
            }
            handler.post(Runnable {
                Toast.makeText(applicationContext, "Service closed", Toast.LENGTH_SHORT).show()
            })
        }
    }

    override fun onLocationChanged(location: Location?) {
        mSharedPreference?.setLastKnownLocation(LatLng(location!!.latitude, location.longitude))
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) { }
    override fun onProviderEnabled(provider: String?) { }
    override fun onProviderDisabled(provider: String?) { }
}