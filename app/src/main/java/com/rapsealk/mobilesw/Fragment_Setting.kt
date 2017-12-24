package com.rapsealk.mobilesw

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.rapsealk.mobilesw.service.CameraObservingService
import com.rapsealk.mobilesw.util.SharedPreferenceManager
import kotlinx.android.synthetic.main.fragment_setting.*

class Fragment_Setting : Fragment() {
    private var mFirebaseAuth: FirebaseAuth? = null

    private val CAMERA_REQUEST_CODE: Int = 10
    private val FINE_LOCATION_CODE: Int = 11
    private var mSharedPreference: SharedPreferenceManager? = null
    var mContext: Context? = null;

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_setting, container, false)
        mContext = container!!.context
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mFirebaseAuth = FirebaseAuth.getInstance()
        val user = mFirebaseAuth?.currentUser


        if (ContextCompat.checkSelfPermission(mContext!!, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.CAMERA)) {
                Toast.makeText(mContext, "카메라 정보를 이용하기 위해서는 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
            ActivityCompat.requestPermissions(activity, Array<String>(1) { android.Manifest.permission.CAMERA }, CAMERA_REQUEST_CODE)
        }


        if (ContextCompat.checkSelfPermission(mContext!!, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(mContext, "GPS 정보를 이용하기 위해서는 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
            ActivityCompat.requestPermissions(activity, Array<String>(1) { android.Manifest.permission.ACCESS_FINE_LOCATION }, FINE_LOCATION_CODE)
        }

        etName.setText(user?.displayName)

        btnName.setOnClickListener { v: View? ->

            val progressDialog = ProgressDialog(getActivity())
            progressDialog.isIndeterminate = true
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progressDialog.setMessage("사용자 이름을 변경하는 중")
            progressDialog.show()

            val newDisplayName = etName.text.toString()

            val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(newDisplayName)
                    .build()

            user?.updateProfile(profileUpdate)
                    ?.addOnCompleteListener(object : OnCompleteListener<Void> {
                       override fun onComplete(task: Task<Void>) {
                            if (task.isSuccessful()) {
                                Logout.performClick()
                            }
                        }
                    })
        }


        mSharedPreference = SharedPreferenceManager.getInstance(getActivity().applicationContext)

        CameraService.isChecked = mSharedPreference!!.getCameraObservingService(false)
        RecallService.isChecked = mSharedPreference!!.getRecallService(false)

        CameraService.setOnCheckedChangeListener { buttonView, isChecked ->
            val serviceIntent = Intent(getActivity().applicationContext, CameraObservingService::class.java)
            if (isChecked) { getActivity().applicationContext.startService(serviceIntent) }
            else { getActivity().applicationContext.stopService(serviceIntent) }
            mSharedPreference!!.setCameraObservingService(isChecked)
        }

        RecallService.setOnCheckedChangeListener { buttonView, isChecked ->
            Log.d("Service", "SwitchRecallService::$isChecked")
            val serviceIntent = Intent(getActivity().applicationContext, RecallService::class.java)
            if (isChecked) getActivity().applicationContext.startService(serviceIntent) else getActivity().applicationContext.stopService(serviceIntent)
            mSharedPreference!!.setRecallService(isChecked)
        }

        Logout.setOnClickListener { v: View? ->
            mFirebaseAuth?.signOut()
            val intent = Intent(getActivity().applicationContext, LoginActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            //finish()
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    /*
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