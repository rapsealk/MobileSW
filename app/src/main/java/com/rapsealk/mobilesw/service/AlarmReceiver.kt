package com.rapsealk.mobilesw.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.rapsealk.mobilesw.SelectPicture

/**
 * Created by LG on 2017-10-30.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(9999)

        var time= intent.getLongExtra("time",0)

        //Log.d("shuffTest", "????????????????????????????????")

        val action = intent.action
        if (YES_ACTION == action) {
            Log.d("shuffTest", "Pressed YES")
            val go = Intent(context, SelectPicture::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            go.putExtra("time",time)
            context.startActivity(go)

        } else if (NO_ACTION == action) {
            Log.d("shuffTest", "Pressed MAYBE")
        }
    }

    companion object {

        val YES_ACTION = "YES_ACTION"
        val NO_ACTION = "NO_ACTION"
    }
}