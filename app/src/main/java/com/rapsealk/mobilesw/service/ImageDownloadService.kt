package com.rapsealk.mobilesw.service

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Environment
import android.os.IBinder
import android.widget.Toast
import com.rapsealk.mobilesw.util.MediaScanner
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by rapsealk on 2017. 11. 14..
 */
class ImageDownloadService() : Service() {

    private var url: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        url = intent?.getStringExtra("url")
        val thread = Thread(ImageDownloader())
        thread.start()
        return super.onStartCommand(intent, flags, startId)
    }

    private inner class ImageDownloader : Runnable {
        override fun run() {
            val mime = url!!.split(".").last().split("?").first()
            val timestamp = System.currentTimeMillis()
            val filename = "$timestamp.$mime"
            ImageDownloadTask(filename).execute(url)
        }
    }

    private inner class ImageDownloadTask(val filename: String) : AsyncTask<String, Int, Bitmap>() {

        override fun doInBackground(vararg params: String?): Bitmap {
            val url = params.get(0)
            return Picasso.with(applicationContext).load(url).get()
        }

        override fun onPostExecute(result: Bitmap?) {
            // super.onPostExecute(result)
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
                MediaScanner(applicationContext, file)
                Toast.makeText(applicationContext, "사진이 저장됐습니다.", Toast.LENGTH_SHORT).show()
            }
            catch (exception: IOException) {
                exception.printStackTrace()
            }
        }
    }
}