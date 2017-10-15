package com.example.lg.tttt

import android.Manifest
import android.Manifest.permission_group.CAMERA
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button

class MainActivity : AppCompatActivity() {

    internal var ID = arrayOf<String>()
    internal var mCamera: Camera? = null
    internal val CAMERA_REQUEST_CODE = 100
    internal val ALERT_REQUEST_CODE = 101
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val b1 = findViewById(R.id.button1) as Button
        val b2 = findViewById(R.id.button2) as Button
        val b = findViewById(R.id.button) as Button
        val APIVersion = android.os.Build.VERSION.SDK_INT



        if (APIVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkCAMERAPermission()) {  //이미 권한이 허가되어 있는지 확인한다. (표.1 로 구현)
                //mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);  //카메라를 open() 할 수 있다.
            } else {  //카메라 권한을 요청한다.
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
            }
        }

        b.setOnClickListener {
            // 서비스 시작하기
            val intent = Intent(applicationContext, //현재제어권자
                    Main2Activity::class.java) // 이동할 컴포넌트
            startActivity(intent) // 서비스 시작
        }

        //
        b1.setOnClickListener {
            // 서비스 시작하기
            Log.d("test", "액티비티-서비스 시작버튼클릭")
            val intent = Intent(applicationContext, //현재제어권자
                    MyService::class.java) // 이동할 컴포넌트
            startService(intent) // 서비스 시작
        }

        b2.setOnClickListener {
            // 서비스 종료하기
            //   Log.d("test", Integer.toString(camera));
            val intent = Intent(
                    applicationContext, //현재제어권자
                    MyService::class.java) // 이동할 컴포넌트
            stopService(intent) // 서비스 종료
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------
    private fun checkCAMERAPermission(): Boolean {

        val result = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)

        return result == PackageManager.PERMISSION_GRANTED

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
        // 권한 요청을 여러가지 했을 경우를 대비해 switch문으로 묶어 관리한다.
            CAMERA_REQUEST_CODE    //권한 요청시 전달했던 '권한 요청'에 대한 식별 코드
            -> if (grantResults.size > 0) {
                val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED  //권한을 허가받았는지 boolean값으로 저장한다.
                if (cameraAccepted) {
                    mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)  //카메라를 open() 할 수 있다.
                } else { //권한 승인을 거절당했다. ㅠ
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(CAMERA)) {  //이 함수는 권한 요청을 실행한 적이 있고, 사용자가 이를 거절했을 때 true를 리턴한다.


                            showMessagePermission("권한 허가를 요청합니다~ 문구", //표.2 로 구현


                                    DialogInterface.OnClickListener { dialog, which ->
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermissions(arrayOf(CAMERA), CAMERA_REQUEST_CODE)  //1)에서 요청했던 함수와 동일
                                        }
                                    })
                            return
                        }
                    }

                }
            }
        }
    }

    private fun showMessagePermission(message: String, okListener: DialogInterface.OnClickListener) {

        android.support.v7.app.AlertDialog.Builder(this)

                .setMessage(message)

                .setPositiveButton("허용", okListener)

                .setNegativeButton("거부", null)

                .create()

                .show()
    }

}
