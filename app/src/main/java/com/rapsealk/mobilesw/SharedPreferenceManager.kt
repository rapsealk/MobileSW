package com.rapsealk.mobilesw

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by rapsealk on 2017. 10. 1..
 */
public class SharedPreferenceManager {

    companion object {

        private var mInstance: SharedPreferenceManager? = null
        private var mSharedPreference: SharedPreferences? = null
        private var mEdit: SharedPreferences.Editor? = null

        private val FILE_NAME = "MobileSWPreference"

        private val CAMERA_OBSERVING_SERVICE = "CAMERA_OBSERVING_SERVICE_ON"

        public fun getInstance(context: Context): SharedPreferenceManager {

            if (mInstance == null) mInstance = SharedPreferenceManager(context)

            return mInstance!!

        }
    }

    private constructor(context: Context) {
        mSharedPreference = context.getSharedPreferences(SharedPreferenceManager.FILE_NAME, Context.MODE_PRIVATE)
        mEdit = mSharedPreference?.edit()
    }

    public fun setCameraObservingService(boolean: Boolean): SharedPreferenceManager {
        mEdit?.putBoolean(CAMERA_OBSERVING_SERVICE, boolean)
        mEdit?.commit()
        return mInstance!!
    }

    public fun getCameraObservingService(boolean: Boolean): Boolean {
        return mSharedPreference!!.getBoolean(CAMERA_OBSERVING_SERVICE, boolean)
    }
}