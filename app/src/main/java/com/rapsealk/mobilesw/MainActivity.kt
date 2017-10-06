package com.rapsealk.mobilesw

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.rapsealk.mobilesw.service.CameraObservingService
import com.rapsealk.mobilesw.service.RecallService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private var isFirstRun = true
    }

    private var mSharedPreference: SharedPreferenceManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(custom_toolbar)

        var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        if (mFirebaseAuth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        if (isFirstRun) {
            isFirstRun = false
            this.onPause()
            startActivity(Intent(this, SplashActivity::class.java))
        }

        mSharedPreference = SharedPreferenceManager.getInstance(this)
        if (mSharedPreference!!.getCameraObservingService(false)) {
            startService(Intent(applicationContext, CameraObservingService::class.java))
        }
        if (mSharedPreference!!.getRecallService(false)) {
            startService(Intent(applicationContext, RecallService::class.java))
        }

        imageButtonWorldPhoto.setOnClickListener { view: View ->
            var intent = Intent(this, WorldPhotoActivity::class.java)
            this.onPause()
            startActivity(intent)
        }

        imageButtonSetting.setOnClickListener { view: View ->
            this.onPause()
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        imageButtonMyPage.setOnClickListener { view: View ->
            this.onPause()
            startActivity(Intent(this, MyPageActivity::class.java))
        }

        imageButtonInfo.setOnClickListener { view: View ->
            var intent = Intent(this, InfoActivity::class.java)
            this.onPause()
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
    }
}

// Extension
fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}