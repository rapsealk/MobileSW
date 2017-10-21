package com.rapsealk.mobilesw.retrofit

import io.reactivex.Observable
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by rapsealk on 2017. 10. 18..
 */
interface CloudMessageService {

    companion object Factory {
        fun create(): CloudMessageService {
            val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl("http://52.78.4.96:3003/")
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            return retrofit.create(CloudMessageService::class.java)
        }
    }

    @POST("fcm/send")
    fun sendMessage(@Body body: SendingMessage): Observable<MessageResponse>
    // fun sendMessage(@Body body: SendingMessage): Call<MessageResponse>
}