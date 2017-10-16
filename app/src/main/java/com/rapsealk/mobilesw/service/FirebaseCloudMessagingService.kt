package com.rapsealk.mobilesw.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService

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
}