package com.rapsealk.mobilesw.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Created by GL552 on 2017-10-01.
 */
class RecallService : Service {

    constructor() : super() {}

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}