package com.rapsealk.mobilesw.util

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Environment
import android.os.Handler
import android.widget.Toast
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by rapsealk on 2017. 11. 14..
 */
// TODO("upgrade to service")
class ImageDownloader : AsyncTask<String, Int, Bitmap> {

    private val progressDialog: ProgressDialog
    private val context: Context
    private val filename: String
    private val handler: Handler

    constructor(context: Context, filename: String): super() {
        this.context = context
        this.filename = filename
        progressDialog = ProgressDialog(context)
        progressDialog.isIndeterminate = true
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setMessage("사진 저장 중")
        handler = Handler()
    }

    override fun doInBackground(vararg params: String?): Bitmap {
        handler.post(Runnable { progressDialog.show() })
        val url = params.get(0)
        return Picasso.with(context).load(url).get()
    }

    override fun onPostExecute(result: Bitmap?) {

        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "PhotoPlace")
        if (!directory.exists()) directory.mkdir()

        val file = File(directory.path + File.separator + filename)
        try {
            file.createNewFile()
            val ostream = FileOutputStream(file)
            val mime = filename.split(".").last()
            when (mime.toLowerCase()) {
                "jpg", "jpeg" -> {
                    result?.compress(Bitmap.CompressFormat.JPEG, 100, ostream)
                }
                "png" -> {
                    result?.compress(Bitmap.CompressFormat.PNG, 100, ostream)
                }
            }
            ostream.flush()
            ostream.close()
            MediaScanner(context, file)
            handler.post(Runnable { progressDialog.dismiss() })
            Toast.makeText(context, "사진이 저장됐습니다.", Toast.LENGTH_SHORT).show()
        }
        catch (exception: IOException) { }
    }

}