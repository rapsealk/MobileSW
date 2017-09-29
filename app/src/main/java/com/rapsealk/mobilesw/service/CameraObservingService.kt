package com.rapsealk.mobilesw.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Camera
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.rapsealk.mobilesw.MainActivity
import com.rapsealk.mobilesw.R

/**
 * Created by rapsealk on 2017. 9. 24..
 */
class CameraObservingService : Service {

    private var cameraOnUse = false
    private var isStop = false

    constructor() : super() {}

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var counter = Thread(CameraObserver())
        counter.start()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        isStop = true
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
            Log.d("CameraObserver", "Run")
            while (!isStop) {
                Log.d("CameraObserver", "inside while loop")
                if (!onCameraUse() && !cameraOnUse) { }
                else if (onCameraUse() && !cameraOnUse) { cameraOnUse = true }
                else if (onCameraUse() && cameraOnUse) { }
                else if (!onCameraUse() && cameraOnUse) {

                    cameraOnUse = false

                    var notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    var pushIntent = Intent()
                    var fullScreenPendingIntent = PendingIntent.getActivity(applicationContext, 0, pushIntent, PendingIntent.FLAG_CANCEL_CURRENT)
                    var builder = Notification.Builder(applicationContext)

                    builder.setFullScreenIntent(fullScreenPendingIntent, true)
                    builder.setSmallIcon(R.mipmap.ic_launcher)
                    builder.setTicker("Ticker")
                    builder.setWhen(System.currentTimeMillis())
                    builder.setContentTitle("ContentTitle")
                    builder.setContentText("ContentText")
                    builder.setAutoCancel(true)
                    builder.setPriority(Notification.PRIORITY_MAX)
                    builder.addAction(android.R.drawable.star_big_on, "On", fullScreenPendingIntent)
                    builder.addAction(android.R.drawable.star_big_off, "Off", fullScreenPendingIntent)

                    pushIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    pushIntent.setClass(applicationContext, MainActivity::class.java)

                    notificationManager.notify(9999, builder.build())

                    break
                }
            }
            handler.post(Runnable {
                Toast.makeText(applicationContext, "Service closed", Toast.LENGTH_SHORT).show()
            })
        }
    }

}