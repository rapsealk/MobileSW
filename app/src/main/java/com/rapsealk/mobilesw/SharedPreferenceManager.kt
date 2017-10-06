package com.rapsealk.mobilesw

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.maps.model.LatLng

/**
 * Created by rapsealk on 2017. 10. 1..
 */
public class SharedPreferenceManager {

    companion object {

        private var mInstance: SharedPreferenceManager? = null
        private var mSharedPreference: SharedPreferences? = null
        private var mEdit: SharedPreferences.Editor? = null

        private val FILE_NAME = "MobileSWPreference"

        private val LAST_KNOWN_LOCATION = "LAST_KNOWN_LOCATION"
        private val CAMERA_OBSERVING_SERVICE = "CAMERA_OBSERVING_SERVICE_ON"
        private val RECALL_SERVICE = "RECALL_SERVICE_ON"

        // Singleton - Anti Pattern
        public fun getInstance(context: Context): SharedPreferenceManager {

            if (mInstance == null) mInstance = SharedPreferenceManager(context)

            return mInstance!!

        }
    }

    private constructor(context: Context) {
        mSharedPreference = context.getSharedPreferences(SharedPreferenceManager.FILE_NAME, Context.MODE_PRIVATE)
        mEdit = mSharedPreference?.edit()
    }

    public fun setLastKnownLocation(latLng: LatLng): SharedPreferenceManager {
        var latitude = latLng.latitude
        var longitude = latLng.longitude
        mEdit?.putString(LAST_KNOWN_LOCATION, "$latitude/$longitude")
        mEdit?.commit()
        return mInstance!!
    }

    public fun getLastKnownLocation(): LatLng {
        var lastKnown = mSharedPreference!!.getString(LAST_KNOWN_LOCATION, "DEFAULT")
        if (lastKnown == null) return LatLng(127.0, 37.0)
        var lastKnowns = lastKnown.split("/")
        var location = LatLng(lastKnowns.get(0).toDouble(), lastKnowns.get(1).toDouble())
        return location
    }

    public fun setCameraObservingService(boolean: Boolean): SharedPreferenceManager {
        mEdit?.putBoolean(CAMERA_OBSERVING_SERVICE, boolean)
        mEdit?.commit()
        return mInstance!!
    }

    public fun getCameraObservingService(boolean: Boolean): Boolean {
        return mSharedPreference!!.getBoolean(CAMERA_OBSERVING_SERVICE, boolean)
    }

    public fun setRecallService(boolean: Boolean): SharedPreferenceManager {
        mEdit?.putBoolean(RECALL_SERVICE, boolean)
        mEdit?.commit()
        return mInstance!!
    }

    public fun getRecallService(boolean: Boolean): Boolean {
        return mSharedPreference!!.getBoolean(RECALL_SERVICE, boolean)
    }
}