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
import android.widget.Toast
import com.rapsealk.mobilesw.MainActivity
import com.rapsealk.mobilesw.R

/**
 * Created by rapsealk on 2017. 9. 24..
 */
class CameraObservingService : Service {

    private var step = 0
    private var isStop = false

    constructor() : super() {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var counter = Thread(Runnable {
            while (!isStop) {
                if (!onCameraUse() && step == 0) { }
                else if (onCameraUse() && step == 0) { step = 1}
                else if (onCameraUse() && step == 1) { }
                else if (!onCameraUse() && step == 1) {
                    step = 0

                    var notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
            Handler().post(Runnable {
                Toast.makeText(applicationContext, "Service closed", Toast.LENGTH_SHORT).show()
            })
        })
        return super.onStartCommand(intent, flags, startId)
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

}