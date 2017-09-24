package com.rapsealk.mobilesw.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Created by rapsealk on 2017. 9. 24..
 */
class CameraObservingService : Service {

    private val step = 0
    private val isStop = false

    private var notificationManager: NotificationManager? = null
    private var notificationBuilder: Notification.Builder? = null
    private var push: Intent? = null
    private var fullScreenPendingIntent: PendingIntent? = null

    constructor() : super() {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var counter = Thread(Runnable {
            while (!isStop) {

            }
        })
        return super.onStartCommand(intent, flags, startId)
    }

}