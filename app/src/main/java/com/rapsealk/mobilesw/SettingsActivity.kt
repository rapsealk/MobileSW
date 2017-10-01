package com.rapsealk.mobilesw

import android.Manifest
import android.Manifest.permission_group.CAMERA
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.rapsealk.mobilesw.service.CameraObservingService
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE: Int = 10
    private var mSharedPreference: SharedPreferenceManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                toast("카메라 정보를 이용하기 위해서는 권한이 필요합니다.")
            }
            ActivityCompat.requestPermissions(this, Array<String>(1) { Manifest.permission.CAMERA }, CAMERA_REQUEST_CODE)
        }

        mSharedPreference = SharedPreferenceManager.getInstance(this)

        if (mSharedPreference!!.getCameraObservingService(true)) {
            startService(Intent(applicationContext, CameraObservingService::class.java))
            switchCameraService.isChecked = true
        }

        switchCameraService.setOnCheckedChangeListener { buttonView, isChecked ->
            var serviceIntent = Intent(applicationContext, CameraObservingService::class.java)
            toast("isChecked: " + isChecked.toString())
            if (isChecked) { startService(serviceIntent) }
            else { stopService(serviceIntent) }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    toast("ACCESS_CAMERA PERMISSION GRANTED")
                else finish()
                return
            }
            else -> {
                finish()
            }
        }
    }
}
