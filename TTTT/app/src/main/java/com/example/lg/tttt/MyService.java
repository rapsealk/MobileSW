package com.example.lg.tttt;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by LG on 2017-09-10.
 */

public class MyService extends Service {
    int step=0;
    boolean isStop=false;

    @Override
    public IBinder onBind(Intent intent) {
        // Service 객체와 (화면단 Activity 사이에서)
        // 통신(데이터를 주고받을) 때 사용하는 메서드
        // 데이터를 전달할 필요가 없으면 return null;
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        // 서비스에서 가장 먼저 호출됨(최초에 한번만)
        Log.d("test", "서비스의 onCreate");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서비스가 호출될 때마다 실행
        Log.d("test", "서비스의 onStartCommand");
        Thread counter = new Thread(new Counter());
        counter.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 서비스가 종료될 때 실행
        isStop=true;
        Log.d("test", "서비스의 onDestroy");
    }
    public boolean isCameraUsebyApp() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (RuntimeException e) {
            return true;
        } finally {
            if (camera != null) camera.release();
        }
        return false;
    }
    private class Counter implements Runnable {
        private int count;
        private Handler handler = new Handler();
        @Override
        public void run() {
            while(true) {
                if ( isStop ) {
                    break;
                }
                if (!isCameraUsebyApp() && step == 0) {
                    Log.d("test", "카메라 OFF1111111");
                } else if (isCameraUsebyApp() && step == 0) {
                    Log.d("test", "카메라 ON111111");
                    step = 1;
                } else if (isCameraUsebyApp() && step == 1) {
                    Log.d("test", "카메라 ON222222");
                } else if (!isCameraUsebyApp() && step == 1) {
                    Log.d("test", "카메라 OFF2222222");
                    step = 0;

                    /*
                    Intent al;
                    al = new Intent(getApplicationContext(), MyAlert.class);
                    al.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(al);
                    */

                    Intent popupIntent = new Intent(getApplicationContext(), MyAlert.class);

                   // popupIntent.putExtras(bun);
                    PendingIntent pie= PendingIntent.getActivity(getApplicationContext(), 0, popupIntent, PendingIntent.FLAG_ONE_SHOT);
                    try {
                        pie.send();
                    } catch (PendingIntent.CanceledException e) {
                       // LogUtil.degug(e.getMessage());
                    }

                    /*
                    AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext())
                            .setTitle("Title")
                            .setMessage("Are you sure?")
                            .create();

                    alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    alertDialog.show();
                    */
                    break;
                }
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "서비스 종료", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
