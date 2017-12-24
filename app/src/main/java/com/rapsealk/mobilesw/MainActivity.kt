package com.rapsealk.mobilesw

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.rapsealk.mobilesw.service.CameraObservingService
import com.rapsealk.mobilesw.service.FirebaseInstanceIDService
import com.rapsealk.mobilesw.service.RecallService
import com.rapsealk.mobilesw.util.SharedPreferenceManager
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private var isFirstRun = true

    private var mSharedPreference: SharedPreferenceManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(custom_toolbar)

        val mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val user = mFirebaseAuth.currentUser

        /*
        if (isFirstRun) {
            isFirstRun = false
            this.onPause()
            startActivity(Intent(this, SplashActivity::class.java))
        }

        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            return finish()
        }
        */

        startService(Intent(applicationContext, FirebaseInstanceIDService::class.java))

        mSharedPreference = SharedPreferenceManager.getInstance(this)
        if (mSharedPreference!!.getCameraObservingService(false)) {
            startService(Intent(applicationContext, CameraObservingService::class.java))
        }
        if (mSharedPreference!!.getRecallService(false)) {
            startService(Intent(applicationContext, RecallService::class.java))
        }

        if (!mSharedPreference!!.isInstanceIdAlive()) {
            val id = FirebaseInstanceId.getInstance().getToken()!!
            Log.d("new token:", id);
            mSharedPreference!!.updateInstanceId(id)
        } else {
            val instanceIdToken = mSharedPreference!!.retrieveInstanceId()!!
            val uid = user!!.uid
            Log.d("instanceIdToken", instanceIdToken)
            Log.d("User", "uid: $uid")
            FirebaseDatabase.getInstance().getReference("users").child(uid).child("instanceIdToken")
                    .setValue(instanceIdToken)
                    .addOnCompleteListener { task: Task<Void> ->
                        if (task.isSuccessful) {
                            Log.d("FirebaseInstanceId", "Update succeed to $instanceIdToken")
                        }
                    }
                    .addOnFailureListener { exception: Exception ->
                        Log.d("FirebaseInstanceId", "Error occured while updating instanceIdToken.")
                        exception.printStackTrace()
                    }
        }

        // TODO: AdMob (https://developers.google.com/admob/android/banner?hl=ko)
        /*
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
         */
        // MobileAds.initialize(this, "ca-app-pub-3646601663753152~4242295983")
        // val adRequest = AdRequest.Builder()
        //         .addTestDevice("11FA7C8BEAD9541214256C4099D5D934")  // Luna S
        //         .addTestDevice("3F0353CEF8C0D94235DC6052DCF0D49A")  // Galaxy S6
        //         .addTestDevice("2CC79B5532C0B9EC2E03486315B181D0")  // Galaxy Note II
        //         .addTestDevice("8BFC47801A471FF1C6D2DE3682F933F3")  // Galaxy S6 Edge
        //         .build()
        // adView.loadAd(adRequest)

        imageButtonWorldPhoto.setOnClickListener { view: View ->
            val intent = Intent(this, WorldPhotoActivity::class.java)
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
            val intent = Intent(this, InfoActivity::class.java)
            this.onPause()
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        stopService(Intent(applicationContext, FirebaseInstanceIDService::class.java))
        super.onDestroy()
    }
}

// Extension
fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}