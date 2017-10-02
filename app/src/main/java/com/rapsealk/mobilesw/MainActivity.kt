package com.rapsealk.mobilesw

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.rapsealk.mobilesw.service.CameraObservingService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var isFirstRun = true

    private var mSharedPreference: SharedPreferenceManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(custom_toolbar)

        var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        if (mFirebaseAuth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        if (isFirstRun) {
            isFirstRun = false
            startActivity(Intent(this, SplashActivity::class.java))
        }

        mSharedPreference = SharedPreferenceManager.getInstance(this)
        if (mSharedPreference!!.getCameraObservingService(true)) {
            startService(Intent(applicationContext, CameraObservingService::class.java))
        }

        imageButtonWorldPhoto.setOnClickListener { view: View ->
            var intent = Intent(this, WorldPhotoActivity::class.java)
            this.onPause()
            startActivity(intent)
        }

        imageButtonSetting.setOnClickListener { view: View ->
            // toast("Setting")
            this.onPause()
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        imageButtonMyPage.setOnClickListener { view: View ->
            // toast("MyPage")
            mFirebaseAuth.signOut()
            var intent = Intent(this, LoginActivity::class.java)
            this.onPause()
            startActivity(intent)
        }

        imageButtonInfo.setOnClickListener { view: View ->
            var intent = Intent(this, InfoActivity::class.java)
            this.onPause()
            startActivity(intent)
        }
    }

    /*
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    toast("CAMERA PERMISSION GRANTED")
                else finish()
                return
            }
            else -> {
                finish()
            }
        }
    }
    */
}

// Extension
fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}