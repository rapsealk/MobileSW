package com.rapsealk.mobilesw.schema

import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

/**
 * Created by rapsealk on 2017. 10. 1..
 */
@IgnoreExtraProperties
data class Photo (
    val comments: Map<String, Comment> = HashMap<String, Comment>(),
    val content: String = "",
    val latitude: Double = 127.0,
    val longitude: Double = 37.0,
    val timestamp: Long = 0,
    val uid: String = "",
    val url: String = ""
) : Serializable