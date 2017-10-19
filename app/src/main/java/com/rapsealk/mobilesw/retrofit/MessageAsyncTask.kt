package com.rapsealk.mobilesw.retrofit

import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import retrofit2.Call
import retrofit2.Response
import java.io.IOException

/**
 * Created by rapsealk on 2017. 10. 19..
 */
public class MessageAsyncTask<T>(val context: Context) : AsyncTask<Call<T>, Void, String>() {

    override fun doInBackground(vararg params: Call<T>?): String {
        val call: Call<T> = params[0]!!
        try {
            val response: Response<T> = call.execute()
            if (response.isSuccessful) {
                if (response.body() != null) return response.code().toString() + " " + response.body().toString()
                else return response.code().toString() + " response.body() is null"
            }
            return response.code().toString() + " response failed"
        }
        catch (exception: IOException) {
            exception.printStackTrace()
            return exception.toString()
        }
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        Toast.makeText(context, result, Toast.LENGTH_LONG)
    }
}