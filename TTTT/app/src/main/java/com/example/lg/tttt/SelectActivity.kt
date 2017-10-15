package com.example.lg.tttt

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class SelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(123456);
        val  REQUEST_TAKE_ALBUM=2002;

        var intent = Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_TAKE_ALBUM);
    }
}
