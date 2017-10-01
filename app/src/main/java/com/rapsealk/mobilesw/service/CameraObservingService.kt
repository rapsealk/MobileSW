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
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import com.rapsealk.mobilesw.R
import com.rapsealk.mobilesw.UploadPhotoActivity

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
            while (!isStop) {
                if (!onCameraUse() && !cameraOnUse) { }
                else if (onCameraUse() && !cameraOnUse) { cameraOnUse = true }
                else if (onCameraUse() && cameraOnUse) { }
                else if (!onCameraUse() && cameraOnUse) {

                    cameraOnUse = false

                    var notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    var uploadImageIntent = Intent(this@CameraObservingService, UploadPhotoActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

                    var uploadImagePendingIntent = PendingIntent.getActivity(this@CameraObservingService, 1, uploadImageIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    var ncbuilder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext)
                            .setFullScreenIntent(uploadImagePendingIntent, true)
                            .setContentIntent(uploadImagePendingIntent)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("ContentTitle")
                            .setContentText("ContextText")
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setAutoCancel(true)
                            .setContentIntent(uploadImagePendingIntent)

                    notificationManager.notify(9999, ncbuilder.build())

                    break
                }
            }
            handler.post(Runnable {
                Toast.makeText(applicationContext, "Service closed", Toast.LENGTH_SHORT).show()
            })
        }
    }

}