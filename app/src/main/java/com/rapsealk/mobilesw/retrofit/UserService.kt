package com.rapsealk.mobilesw.retrofit

import com.rapsealk.mobilesw.retrofit.response.DataResponse
import com.rapsealk.mobilesw.schema.User
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by rapsealk on 2017. 10. 26..
 */
interface UserService {

    companion object Factory {
        fun create(): UserService {
            val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl("http://52.78.4.96:3003/")
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            return retrofit.create(UserService::class.java)
        }
    }

    @GET("users/{uid}")
    fun getUser(@Path("uid") uid: String): Observable<DataResponse<User>>
}