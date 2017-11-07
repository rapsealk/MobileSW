package com.rapsealk.mobilesw.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import java.lang.Exception

/**
 * Created by rapsealk on 2017. 11. 7..
 */
class PostDeleteService : Service() {

    private var mPath: String = ""
    // private var mResultMessage: String = ""

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        mPath = intent.getStringExtra("path")
        val thread = Thread(PostDeleteDelegator())
        thread.start()
        return super.onStartCommand(intent, flags, startId)
    }

    private inner class PostDeleteDelegator : Runnable {

        private val handler: Handler = Handler()

        override fun run() {
            FirebaseStorage.getInstance().getReference(mPath).delete()
                    .addOnCompleteListener { task: Task<Void> ->
                        // mResultMessage = "successfully deleted"
                    }
                    .addOnFailureListener { exception: Exception ->
                        // mResultMessage = "failed to delete"
                    }
            handler.post(Runnable {
                // Toast.makeText(applicationContext, mResultMessage, Toast.LENGTH_SHORT).show()
            })
        }
    }
}