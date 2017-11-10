package com.rapsealk.mobilesw

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.rapsealk.mobilesw.service.CameraObservingService
import com.rapsealk.mobilesw.service.RecallService
import com.rapsealk.mobilesw.util.SharedPreferenceManager
import kotlinx.android.synthetic.main.activity_fragment.*

class FragmentActivity : AppCompatActivity() {

    private var mSharedPreference: SharedPreferenceManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment)

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

        /*
        MobileAds.initialize(this, "ca-app-pub-3646601663753152~4242295983")
        val adRequest = AdRequest.Builder()
                .addTestDevice("11FA7C8BEAD9541214256C4099D5D934")  // Luna S
                .addTestDevice("3F0353CEF8C0D94235DC6052DCF0D49A")  // Galaxy S6
                .addTestDevice("2CC79B5532C0B9EC2E03486315B181D0")  // Galaxy Note II
                .addTestDevice("8BFC47801A471FF1C6D2DE3682F933F3")  // Galaxy S6 Edge
                .build()
        adView.loadAd(adRequest)
        */

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val fragment = Fragment_WorldPhoto()
        fragmentTransaction.replace(R.id.please, fragment)
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit()



        ButtonWorldPhoto.setOnClickListener { view: View ->
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            val fragment = Fragment_WorldPhoto()
            fragmentTransaction.replace(R.id.please, fragment)
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit()

        }

        ButtonSetting.setOnClickListener { view: View ->
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            val fragment = Fragment_Setting()
            fragmentTransaction.replace(R.id.please, fragment)
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit()
        }

        ButtonMyPage.setOnClickListener { view: View ->
          //  this.onPause()
           // val fm = supportFragmentManager
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            val fragment = Fragment_MyPage()
            fragmentTransaction.replace(R.id.please, fragment)
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit()
           // startActivity(Intent(this, MyPageActivity::class.java))
        }

        ButtonInfo.setOnClickListener { view: View ->
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            val fragment = Fragment_Info()
            fragmentTransaction.replace(R.id.please, fragment)
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit()
        }

    }
    fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }
    /*
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
    */
}
