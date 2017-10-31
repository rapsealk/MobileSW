package com.rapsealk.mobilesw.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.PowerManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rapsealk.mobilesw.PostActivity
import com.rapsealk.mobilesw.R
import com.rapsealk.mobilesw.schema.Photo

/**
 * Created by rapsealk on 2017. 10. 16..
 */
public class FirebaseCloudMessagingService : FirebaseMessagingService {

    private var user: FirebaseUser? = null
    private var uid: String = ""

    constructor(): super() { }

    override fun onCreate() {
        super.onCreate()
        user = FirebaseAuth.getInstance().currentUser
        if (user == null) return stopSelf()
        uid = user!!.uid
        FirebaseMessaging.getInstance().subscribeToTopic(uid)
    }

    override fun onDestroy() {
        super.onDestroy()
        FirebaseMessaging.getInstance().unsubscribeFromTopic(uid)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        val postId = data.getValue("post").toLong()
        val message = data.getValue("message")
        sendNotification(postId, message)
    }

    private fun sendNotification(postId: Long, message: String) {
        val mFirebaseDatabase = FirebaseDatabase.getInstance()
        mFirebaseDatabase.getReference("photos/$postId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot?) {
                val photo = snapshot!!.getValue<Photo>(Photo::class.java)
                val intent = Intent(this@FirebaseCloudMessagingService, PostActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .putExtra("SerializedData", photo)
                val pendingIntent = PendingIntent.getActivity(this@FirebaseCloudMessagingService, 125, intent, PendingIntent.FLAG_ONE_SHOT)
                val nBuilder = NotificationCompat.Builder(this@FirebaseCloudMessagingService)
                        .setSmallIcon(R.drawable.ic_send_black_48dp)
                        .setContentTitle("PhotoPlace")
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setPriority(Notification.PRIORITY_MAX)
                val nManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                nManager.notify(999, nBuilder.build())

                // Wake-lock
                val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                if (!powerManager.isScreenOn) {  // FIXME isInteractive on API 20
                    val wakeLock = powerManager.newWakeLock(
                            PowerManager.FULL_WAKE_LOCK or
                                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                                    PowerManager.ON_AFTER_RELEASE, "WakeLock"
                    )
                    wakeLock.acquire(3000)
                    val wakeLockCPU = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CPULock")
                    wakeLockCPU.acquire(3000)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("FirebaseCloudMessage", error.message)
                Toast.makeText(this@FirebaseCloudMessagingService, "게시물을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}