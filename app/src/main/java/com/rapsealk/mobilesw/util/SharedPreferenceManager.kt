package com.rapsealk.mobilesw.util

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

        private val INSTANCE_ID = "INSTANCE_ID"
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
        mSharedPreference = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        mEdit = mSharedPreference?.edit()
    }

    public fun updateInstanceId(id: String) {
        mEdit?.putString(INSTANCE_ID, id)
    }

    public fun retrieveInstanceId(): String? {
        return mSharedPreference!!.getString(INSTANCE_ID, null)
    }

    public fun isInstanceIdAlive(): Boolean {
        return mSharedPreference!!.getString(INSTANCE_ID, null) != null
    }

    public fun setLastKnownLocation(latLng: LatLng): SharedPreferenceManager {
        val latitude = latLng.latitude
        val longitude = latLng.longitude
        mEdit?.putString(LAST_KNOWN_LOCATION, "$latitude/$longitude")
        mEdit?.commit()
        return mInstance!!
    }

    public fun getLastKnownLocation(): LatLng? {
        val lastKnown = mSharedPreference!!.getString(LAST_KNOWN_LOCATION, null)
        if (lastKnown == null) return null // LatLng(127.0, 37.0)
        val lastKnowns = lastKnown.split("/")
        val location = LatLng(lastKnowns.get(0).toDouble(), lastKnowns.get(1).toDouble())
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