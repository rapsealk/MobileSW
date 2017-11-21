package com.rapsealk.mobilesw.service

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.google.firebase.database.*
import com.rapsealk.mobilesw.MainActivity
import com.rapsealk.mobilesw.R

/**
 * Created by GL552 on 2017-10-01.
 */
class RecallService : Service, LocationListener {

    private var isOn = true
    private var lastLatitude: Double = -10000.0
    private var lastLongitude: Double = -10000.0

    private var mLocationManager: LocationManager? = null
    private var mFirebaseDatabase: FirebaseDatabase? = null
    private var ref: DatabaseReference? = null

    private var notificationManager: NotificationManager? = null

    constructor() : super() {}

    override fun onCreate() {
        super.onCreate()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            isOn = false
            Toast.makeText(applicationContext, "RecallService는 위치 정보에 대한 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mLocationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 1f, this)
        mLocationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 1f, this)

        mFirebaseDatabase = FirebaseDatabase.getInstance()
        ref = mFirebaseDatabase?.getReference("photos")

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val observer = Thread(LocationObserver())
        observer.start()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        isOn = false
    }

    private inner class LocationObserver : Runnable {

        override fun run() {

            while (isOn) { }
            stopLocationUpdates()
        }
    }

    override fun onLocationChanged(location: Location?) {

        val latitude = location?.latitude!!
        val longitude = location.longitude

        if ((Math.abs(latitude.minus(lastLatitude)) < 0.0005).and(Math.abs(longitude.minus(lastLongitude)) < 0.0005)) return

        ref?.orderByChild("latitude")
                ?.startAt(latitude.minus(0.0005))
                ?.endAt(latitude.plus(0.0005))
                ?.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot?) {

                snapshot?.ref?.orderByChild("longitude")
                        ?.startAt(longitude.minus(0.0005))
                        ?.endAt(longitude.plus(0.0005))
                        ?.addListenerForSingleValueEvent(object : ValueEventListener {

                            override fun onDataChange(snapshot: DataSnapshot?) {

                                val count = snapshot!!.childrenCount

                                if (count > 0) {
                                    lastLatitude = latitude
                                    lastLongitude = longitude
                                    val recallIntent = Intent(this@RecallService, MainActivity::class.java)
                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    val recallPendingIntent = PendingIntent.getActivity(this@RecallService, 1, recallIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    val ncbuilder = NotificationCompat.Builder(applicationContext)
                                            .setFullScreenIntent(recallPendingIntent, true)
                                            .setContentIntent(recallPendingIntent)
                                            .setSmallIcon(R.drawable.ic_instagram)
                                            .setContentTitle("이곳에서 $count 장의 기록이 있습니다.")
                                            .setContentText("지금 포플에서 확인해보세요! ($latitude, $longitude)")
                                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                                            .setAutoCancel(true)

                                    notificationManager?.notify(7150, ncbuilder.build())
                                }
                            }

                            override fun onCancelled(error: DatabaseError?) { }
                        })
            }

            override fun onCancelled(error: DatabaseError?) { }
        })
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) { }

    override fun onProviderEnabled(provider: String?) { }

    override fun onProviderDisabled(provider: String?) {
        isOn = false
    }

    fun stopLocationUpdates() {
        mLocationManager?.removeUpdates(this)
    }
}