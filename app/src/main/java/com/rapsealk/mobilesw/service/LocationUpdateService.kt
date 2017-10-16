package com.rapsealk.mobilesw.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Created by rapsealk on 2017. 10. 16..
 */
public class LocationUpdateService : Service {

    constructor() : super() {}

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}