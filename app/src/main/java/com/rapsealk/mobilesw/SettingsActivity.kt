package com.rapsealk.mobilesw

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.rapsealk.mobilesw.service.CameraObservingService
import com.rapsealk.mobilesw.service.RecallService
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    private var mFirebaseAuth: FirebaseAuth? = null

    private val CAMERA_REQUEST_CODE: Int = 10
    private val FINE_LOCATION_CODE: Int = 11
    private var mSharedPreference: SharedPreferenceManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        mFirebaseAuth = FirebaseAuth.getInstance()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                toast("카메라 정보를 이용하기 위해서는 권한이 필요합니다.")
            }
            ActivityCompat.requestPermissions(this, Array<String>(1) { Manifest.permission.CAMERA }, CAMERA_REQUEST_CODE)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                toast("GPS 정보를 이용하기 위해서는 권한이 필요합니다.")
            }
            ActivityCompat.requestPermissions(this, Array<String>(1) { Manifest.permission.ACCESS_FINE_LOCATION }, FINE_LOCATION_CODE)
        }

        mSharedPreference = SharedPreferenceManager.getInstance(this)

        switchCameraService.isChecked = mSharedPreference!!.getCameraObservingService(false)
        switchRecallService.isChecked = mSharedPreference!!.getRecallService(false)

        switchCameraService.setOnCheckedChangeListener { buttonView, isChecked ->
            var serviceIntent = Intent(applicationContext, CameraObservingService::class.java)
            if (isChecked) { startService(serviceIntent) }
            else { stopService(serviceIntent) }
            mSharedPreference!!.setCameraObservingService(isChecked)
        }

        switchRecallService.setOnCheckedChangeListener { buttonView, isChecked ->
            var serviceIntent = Intent(applicationContext, RecallService::class.java)
            if (isChecked) startService(serviceIntent) else stopService(serviceIntent)
            mSharedPreference!!.setRecallService(isChecked)
        }

        btnLogout.setOnClickListener { v: View? ->
            mFirebaseAuth?.signOut()
            var intent = Intent(this, LoginActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
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
            FINE_LOCATION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    toast("FINE_LOCATION PERMISSION GRANTED")
                else finish()
                return
            }
            else -> {
                finish()
            }
        }
    }
}
