package com.rapsealk.mobilesw

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE: Int = 10
    private val FINE_LOCATION_CODE: Int = 11

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        /*
        if (checkSTORAGEPermission()) {  //이미 권한이 허가되어 있는지 확인한다. (표.1 로 구현)
            //mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);  //카메라를 open() 할 수 있다.
        } else {  //카메라 권한을 요청한다.
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), FINE_LOCATION_CODE)
        }

        if (checkCAMERAPermission()) {  //이미 권한이 허가되어 있는지 확인한다. (표.1 로 구현)
            //mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);  //카메라를 open() 할 수 있다.
        } else {  //카메라 권한을 요청한다.
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        }
        */


        val mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val user = mFirebaseAuth.currentUser

        var handler = Handler()
        handler.postDelayed(Runnable {
            if (user == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }else{
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }, 2000)


    }

    private fun checkCAMERAPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)
        return result == PackageManager.PERMISSION_GRANTED
    }
    private fun checkSTORAGEPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
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
