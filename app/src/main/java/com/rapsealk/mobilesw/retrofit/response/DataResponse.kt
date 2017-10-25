package com.rapsealk.mobilesw.retrofit.response

/**
 * Created by rapsealk on 2017. 10. 26..
 */
data class DataResponse<T> (
        val result: Int,
        val data: T,
        val error: String = ""
)