package com.rapsealk.mobilesw

import android.widget.Toast
import com.google.firebase.database.*
/**
 * Created by rapsealk on 2017. 9. 15..
 */
class FirebaseDatabaseManager {

    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var ref: DatabaseReference? = null

    constructor(route: String) {
        ref = db.getReference(route)
        ref!!.addListenerForSingleValueEvent(CustomValueEventListener())
    }

    inner class CustomValueEventListener : ValueEventListener {

        override fun onDataChange(snapshot: DataSnapshot?) {
            var data = snapshot!!.value
        }

        override fun onCancelled(error: DatabaseError?) {

        }

    }

}