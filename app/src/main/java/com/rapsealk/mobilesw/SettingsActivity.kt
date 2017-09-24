package com.rapsealk.mobilesw

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.rapsealk.mobilesw.service.CameraObservingService
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    private val serviceIntent = Intent(applicationContext, CameraObservingService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        switchCameraService.setOnCheckedChangeListener { buttonView, isChecked ->
            // var serviceIntent = Intent(applicationContext, CameraObservingService::class.java)
            if (isChecked) { startService(serviceIntent) }
            else { stopService(serviceIntent) }
        }
    }
}
