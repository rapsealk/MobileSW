package com.rapsealk.mobilesw.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

/**
 * Created by rapsealk on 2017. 10. 16..
 */
public class FirebaseInstanceIDService : FirebaseInstanceIdService {

    constructor(): super() {}

    override fun onTokenRefresh() {
        // super.onTokenRefresh()
        var refreshedToken = FirebaseInstanceId.getInstance().token
        Log.d("FirebaseInstanceId", "Refreshed token: $refreshedToken")

        var user = FirebaseAuth.getInstance().currentUser
        var uid = user?.uid
        FirebaseDatabase.getInstance().getReference("users/$uid/instanceIdToken")
                .setValue(refreshedToken)
    }
}