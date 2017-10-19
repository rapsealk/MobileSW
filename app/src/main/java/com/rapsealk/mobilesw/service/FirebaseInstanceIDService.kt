package com.rapsealk.mobilesw.service

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import java.lang.Exception

/**
 * Created by rapsealk on 2017. 10. 16..
 */
public class FirebaseInstanceIDService : FirebaseInstanceIdService {

    constructor(): super() {}

    override fun onTokenRefresh() {
        // super.onTokenRefresh()
        val refreshedToken = FirebaseInstanceId.getInstance().token
        Log.d("FirebaseInstanceId", "Refreshed token: $refreshedToken")

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val uid = user.uid
            Log.d("User", "uid: $uid")
            FirebaseDatabase.getInstance().getReference("users/$uid/instanceIdToken")
                    .setValue(refreshedToken)
                    .addOnCompleteListener { task: Task<Void> ->
                        if (task.isSuccessful) {
                            Log.d("FirebaseInstanceId", "Updated to $refreshedToken")
                        }
                    }
                    .addOnFailureListener { exception: Exception ->
                        Log.d("FirebaseInstanceId", "Error occured while updating token")
                        exception.printStackTrace()
                    }
        }
    }
}