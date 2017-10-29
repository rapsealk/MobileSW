package com.example.lg.tttt

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.hardware.Camera
import android.os.Handler
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast



/**
 * Created by LG on 2017-09-10.
 */

class MyService : Service() {
    internal var step = 0
    internal var isStop = false
    internal var nm: NotificationManager? = null
    internal var builder: Notification.Builder? = null
    internal var push: Intent? = null
    internal var fullScreenPendingIntent: PendingIntent? = null
    var t : Long=0

    override fun onBind(intent: Intent): IBinder? {
        // Service 객체와 (화면단 Activity 사이에서)
        // 통신(데이터를 주고받을) 때 사용하는 메서드
        // 데이터를 전달할 필요가 없으면 return null;
        return null
    }

    override fun onCreate() {
        super.onCreate()
        // 서비스에서 가장 먼저 호출됨(최초에 한번만)
        Log.d("test", "서비스의 onCreate")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // 서비스가 호출될 때마다 실행
        Log.d("test", "서비스의 onStartCommand")
        val counter = Thread(Counter())
        counter.start()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 서비스가 종료될 때 실행
        isStop = true
        Log.d("test", "서비스의 onDestroy")
    }


    val isCameraUsebyApp: Boolean
        get() {
            var camera: Camera? = null
            try {
                camera = Camera.open()
            } catch (e: RuntimeException) {
                return true
            } finally {
                if (camera != null) camera.release()
            }
            return false
        }

    private inner class Counter : Runnable {
        private val count: Int = 0
        private val handler = Handler()
        override fun run() {
            while (true) {
                if (isStop) {
                    break
                }
                if (!isCameraUsebyApp && step == 0) {
                 //   Log.d("test", "카메라 OFF1111111")
                } else if (isCameraUsebyApp && step == 0) {
                 //   Log.d("test", "카메라 ON111111")
                    t = System.currentTimeMillis()

                    step = 1
                } else if (isCameraUsebyApp && step == 1) {
                  //  Log.d("test", "카메라 ON222222")
                } else if (!isCameraUsebyApp && step == 1) {
                  //  Log.d("test", "카메라 OFF2222222")
                    step = 0

                    val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA)

                    // 쿼리 수행
                    val imageCursor : Cursor? = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                            MediaStore.Images.Media.DATE_TAKEN + " >=" + t, null, MediaStore.Images.Media.DATE_ADDED + " desc ")
                    /*
        Cursor imageCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                MediaStore.Images.Media.DATE_TAKEN +" >= ? and"
                        + MediaStore.Images.Media.DATE_TAKEN + "<=?",
                new String[] {start + "", end+ ""}, MediaStore.Images.Media.DATE_ADDED + " desc ");
        */

                    if (imageCursor == null){
                        continue
                    }

                    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val push = Intent()
                    val fullScreenPendingIntent = PendingIntent.getActivity(applicationContext, 0, push, PendingIntent.FLAG_CANCEL_CURRENT)

                    val select = Intent(applicationContext,AlarmReceiver::class.java)
                    select.setAction("YES_ACTION")
                    select.putExtra("time",t)
                    val selectPendingIntent = PendingIntent.getBroadcast(applicationContext, 0, select, PendingIntent.FLAG_UPDATE_CURRENT)

                    val cancel = Intent(applicationContext,AlarmReceiver::class.java)
                    cancel.setAction("NO_ACTION")
                    val cancelPendingIntent = PendingIntent.getBroadcast(applicationContext, 123456, cancel, PendingIntent.FLAG_UPDATE_CURRENT)

                    val builder = Notification.Builder(applicationContext)
                    builder.setFullScreenIntent(fullScreenPendingIntent, true)
                    builder.setSmallIcon(R.mipmap.ic_launcher)
                    builder.setTicker("Test1") //** 이 부분은 확인 필요
                    builder.setWhen(System.currentTimeMillis())
                    builder.setContentTitle("Test2") //** 큰 텍스트로 표시
                    builder.setContentText("Test3") //** 작은 텍스트로 표시
                    builder.setAutoCancel(true)
                    builder.setPriority(Notification.PRIORITY_MAX) //** MAX 나 HIGH로 줘야 가능함
                    //** Intent와 PendingIntent를 추가해 주는 것으로 헤드업 알림이 가능
                    //** 없을 경우 이전 버전의 Notification과 동일
                    builder.addAction(android.R.drawable.star_on, "올리기", selectPendingIntent)
                    builder.addAction(android.R.drawable.star_off, "닫기", cancelPendingIntent)


                    push.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    push.setClass(applicationContext, MainActivity::class.java)

                    // builder.setFullScreenIntent(fullScreenPendingIntent, true);
                    //** 여기까지 헤드업 알림을 사용하기 위한 필수 조건!

                    nm.notify(123456, builder.build())

                 //   break
                }
            }
            handler.post { Toast.makeText(applicationContext, "서비스 종료", Toast.LENGTH_SHORT).show() }
        }
    }
}
