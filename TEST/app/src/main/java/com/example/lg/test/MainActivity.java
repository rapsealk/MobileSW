package com.example.lg.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

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
                Log.d("test", "액티비티-서비스 종료버튼클릭");
                Intent intent = new Intent(
                        getApplicationContext(),//현재제어권자
                        MyService.class); // 이동할 컴포넌트
                stopService(intent); // 서비스 종료
            }
        });
    } // end of onCreate
} // end of class
