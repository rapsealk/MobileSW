package com.example.lg.tttt;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
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
    NotificationManager nm;
    Notification.Builder builder;
    Intent push;
    PendingIntent fullScreenPendingIntent;

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


                    /*///////////////////

                    Intent popupIntent = new Intent(getApplicationContext(), MyAlert.class);
                   // popupIntent.putExtras(bun);
                    PendingIntent pie= PendingIntent.getActivity(getApplicationContext(), 0, popupIntent, PendingIntent.FLAG_ONE_SHOT);
                    try {
                        pie.send();
                    } catch (PendingIntent.CanceledException e) {
                       // LogUtil.degug(e.getMessage());
                    */

                    /*
                    AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext())
                            .setTitle("Title")
                            .setMessage("Are you sure?")
                            .create();

                    alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    alertDialog.show();
                    */

                    /*
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    Intent intent = new Intent();
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification.Builder builder = new Notification.Builder(getApplicationContext());
                    builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), android.R.drawable.star_on));
                    builder.setSmallIcon(android.R.drawable.star_on);
                    builder.setTicker("알람 간단한 설명");
                    builder.setContentTitle("알람 제목");
                    builder.setContentText("알람 내용");
                    builder.setWhen(System.currentTimeMillis());
                    builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                    builder.setContentIntent(pendingIntent);
                    builder.setAutoCancel(true);
                    builder.setNumber(999);
                    builder.addAction(android.R.drawable.star_on, "반짝", pendingIntent);
                    builder.addAction(android.R.drawable.star_off, "번쩍", pendingIntent);
                    notificationManager.notify(0, builder.build());
                    */



                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    Intent push=new Intent();
                    PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, push, PendingIntent.FLAG_CANCEL_CURRENT);
                    Notification.Builder builder = new Notification.Builder(getApplicationContext());


                    builder.setFullScreenIntent(fullScreenPendingIntent, true);
                    builder.setSmallIcon(R.mipmap.ic_launcher);
                    builder.setTicker("Test1"); //** 이 부분은 확인 필요
                    builder.setWhen(System.currentTimeMillis());
                    builder.setContentTitle("Test2"); //** 큰 텍스트로 표시
                    builder.setContentText("Test3"); //** 작은 텍스트로 표시
                    builder.setAutoCancel(true);
                    builder.setPriority(Notification.PRIORITY_MAX); //** MAX 나 HIGH로 줘야 가능함
                    //** Intent와 PendingIntent를 추가해 주는 것으로 헤드업 알림이 가능
                    //** 없을 경우 이전 버전의 Notification과 동일
                    builder.addAction(android.R.drawable.star_on, "반짝", fullScreenPendingIntent);
                    builder.addAction(android.R.drawable.star_off, "번쩍", fullScreenPendingIntent);

                    push.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    push.setClass(getApplicationContext(), MainActivity.class);

                   // builder.setFullScreenIntent(fullScreenPendingIntent, true);
                    //** 여기까지 헤드업 알림을 사용하기 위한 필수 조건!

                    nm.notify(123456, builder.build());

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
