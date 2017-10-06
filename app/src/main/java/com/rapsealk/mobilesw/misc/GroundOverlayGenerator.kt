package com.rapsealk.mobilesw.misc

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.squareup.picasso.Picasso

/**
 * Created by rapsealk on 2017. 10. 7..
 */
class GroundOverlayGenerator : AsyncTask<String, Int, BitmapDescriptor> {

    private val context: Context
    private var bitmapDescriptor: BitmapDescriptor? = null

    constructor(context: Context) : super() { this.context = context }

    override fun doInBackground(vararg params: String?): BitmapDescriptor {
        var url = params.get(0)
        bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(Picasso.with(context).load(url).get())
        return bitmapDescriptor!!
    }

    override fun onPostExecute(result: BitmapDescriptor?) {

    }
}