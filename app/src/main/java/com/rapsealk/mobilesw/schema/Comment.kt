package com.rapsealk.mobilesw.schema

import java.io.Serializable

/**
 * Created by rapsealk on 2017. 10. 2..
 */
data class Comment(
        val comment: String = "",
        val timestamp: Long = 0,
        val uid: String = ""
) : Serializable