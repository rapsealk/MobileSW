package com.example.lg.tttt;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import static android.Manifest.permission_group.CAMERA;

public class MainActivity extends AppCompatActivity {

    String ID[]={};
    Camera mCamera = null;
    final int CAMERA_REQUEST_CODE = 100;
    final int ALERT_REQUEST_CODE = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 서비스 Service - 안드로이드의 4대 컴포넌트 중 하나
        //     화면이 없이 동작함
        // 보통 Activity 에서 호출되어 시작함

        // 1. 사용할 Service (*.java)를 만든다
        // 2. AndroidManifest.xml 에 Service를 등록한다
        // 3. Service 를 시작하도록 호출한다

        Button b1 = (Button) findViewById(R.id.button1);
        Button b2 = (Button) findViewById(R.id.button2);


        //

        int APIVersion = android.os.Build.VERSION.SDK_INT;



        if (APIVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkCAMERAPermission()) {  //이미 권한이 허가되어 있는지 확인한다. (표.1 로 구현)
                //mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);  //카메라를 open() 할 수 있다.
            } else {  //카메라 권한을 요청한다.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            }
        }

         //



        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // 서비스 시작하기
                Log.d("test", "액티비티-서비스 시작버튼클릭");
                Intent intent = new Intent(
                        getApplicationContext(),//현재제어권자
                        MyService.class); // 이동할 컴포넌트
                startService(intent); // 서비스 시작
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // 서비스 종료하기
             //   Log.d("test", Integer.toString(camera));
                Intent intent = new Intent(
                        getApplicationContext(),//현재제어권자
                        MyService.class); // 이동할 컴포넌트
                stopService(intent); // 서비스 종료
            }
        });



    }
    private boolean checkCAMERAPermission() {

        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);

        return result == PackageManager.PERMISSION_GRANTED;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {      // 권한 요청을 여러가지 했을 경우를 대비해 switch문으로 묶어 관리한다.
            case CAMERA_REQUEST_CODE:    //권한 요청시 전달했던 '권한 요청'에 대한 식별 코드
                if (grantResults.length > 0) {
                    boolean cameraAccepted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);  //권한을 허가받았는지 boolean값으로 저장한다.
                    if (cameraAccepted) {
                        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);  //카메라를 open() 할 수 있다.
                    }
                    else { //권한 승인을 거절당했다. ㅠ
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {  //이 함수는 권한 요청을 실행한 적이 있고, 사용자가 이를 거절했을 때 true를 리턴한다.


                                showMessagePermission("권한 허가를 요청합니다~ 문구",   //표.2 로 구현


                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{CAMERA}, CAMERA_REQUEST_CODE);  //1)에서 요청했던 함수와 동일
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }
                break;
        }
    }

    private void showMessagePermission(String message, DialogInterface.OnClickListener okListener) {

        new android.support.v7.app.AlertDialog.Builder(this)

                .setMessage(message)

                .setPositiveButton("허용", okListener)

                .setNegativeButton("거부", null)

                .create()

                .show();
    }

}
