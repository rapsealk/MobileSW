package com.rapsealk.mobilesw

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
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
    var mFragmentId: Int = 0
    var mLastFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment)

        val navigation = findViewById(R.id.navigation) as BottomNavigationView
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
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

        mFragmentId = 1
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val fragment = Fragment_WorldPhoto()
        fragmentTransaction.replace(R.id.please, fragment)
        // fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit()


/*
        ButtonWorldPhoto.setOnClickListener { view: View ->
            frag=1
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            val fragment = Fragment_WorldPhoto()
            fragmentTransaction.replace(R.id.please, fragment)
          //  fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit()

        }

        ButtonSetting.setOnClickListener { view: View ->
            frag=3
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            val fragment = Fragment_Setting()
            fragmentTransaction.replace(R.id.please, fragment)
          //  fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit()
        }

        ButtonMyPage.setOnClickListener { view: View ->
          //  this.onPause()
           // val fm = supportFragmentManager
            frag=2
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            val fragment = Fragment_MyPage()
            fragmentTransaction.replace(R.id.please, fragment)
          //  fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit()
           // startActivity(Intent(this, MyPageActivity::class.java))
        }

        ButtonInfo.setOnClickListener { view: View ->
            frag=4
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            val fragment = Fragment_Info()
            fragmentTransaction.replace(R.id.please, fragment)
        //    fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit()
        }
*/
    }
    fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()

        val navigation = findViewById(R.id.navigation) as BottomNavigationView
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val fragments = supportFragmentManager.fragments
        if (fragments != null) {
            for (fragment in fragments) {
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        if (mLastFragment != null) fragmentTransaction.remove(mLastFragment)

        when (item.itemId) {
            R.id.navigation_worldphoto -> {
                mFragmentId = 1
                mLastFragment = Fragment_WorldPhoto()
                fragmentTransaction.replace(R.id.please, mLastFragment)
                fragmentTransaction.commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_mypage -> {
                mFragmentId = 2
                mLastFragment = Fragment_MyPage()
                fragmentTransaction.replace(R.id.please, mLastFragment)
                fragmentTransaction.commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_setting -> {
                mFragmentId = 3
                mLastFragment = Fragment_Setting()
                fragmentTransaction.replace(R.id.please, mLastFragment)
                fragmentTransaction.commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_info -> {
                mFragmentId = 4
                mLastFragment = Fragment_Info()
                fragmentTransaction.replace(R.id.please, mLastFragment)
                fragmentTransaction.commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onBackPressed() {
        if (mFragmentId != 1) {
            mFragmentId = 1
            navigation.getMenu().findItem(R.id.navigation_worldphoto).setChecked(true)
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.please, Fragment_WorldPhoto())
            fragmentTransaction.commit()
        } else {
            super.onBackPressed()
        }

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
