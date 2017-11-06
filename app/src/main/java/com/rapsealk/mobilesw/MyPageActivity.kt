package com.rapsealk.mobilesw

import android.Manifest
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ViewSwitcher
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rapsealk.mobilesw.util.MediaScanner
import com.rapsealk.mobilesw.schema.Photo
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import kotlinx.android.synthetic.main.activity_my_page.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MyPageActivity : AppCompatActivity() {

    private val WRITE_EXTERNAL_STORAGE_CODE: Int = 10001

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseDatabase: FirebaseDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                toast("이미지를 저장하기 위해서는 권한이 필요합니다.")
            }
            ActivityCompat.requestPermissions(this, Array<String>(1) { Manifest.permission.WRITE_EXTERNAL_STORAGE }, WRITE_EXTERNAL_STORAGE_CODE)
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.isIndeterminate = true
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setMessage("내 사진들 불러오는 중")

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        val user = mFirebaseAuth?.currentUser
        val uid = user?.uid

        btnBack.setOnClickListener { v: View? ->
            finish()
        }

        progressDialog.show()
        val ref = mFirebaseDatabase?.getReference("users")
        // TODO : add a descending element
        ref?.child("$uid/photos")?.orderByChild("timestamp")
                ?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot?) {

                        val children = snapshot!!.children.reversed()
                        val lastIndex = children.count() - 1

                        var horizontalLayout = LinearLayout(this@MyPageActivity)
                        horizontalLayout.layoutMode = LinearLayout.HORIZONTAL

                        children.forEachIndexed { index, child ->
                            val photo = child.getValue<Photo>(Photo::class.java)
                            val viewSwitcher = ViewSwitcher(this@MyPageActivity)
                            val progressBar = ProgressBar(this@MyPageActivity)
                            progressBar.isIndeterminate = true
                            progressBar.visibility = ProgressBar.VISIBLE
                            viewSwitcher.addView(progressBar)
                            val imageView = ImageView(this@MyPageActivity)
                            viewSwitcher.addView(imageView)
                            Picasso.with(this@MyPageActivity)
                                    .load(photo.url)
                                    .transform(object: Transformation {
                                        override fun key(): String = ""
                                        override fun transform(source: Bitmap): Bitmap {

                                            val result = Bitmap.createScaledBitmap(source, 480, 640, false)

                                            // TODO : DOWNLOAD
                                            imageView.setOnClickListener { v: View? ->
                                                val mime = photo.url.split(".").last().split("?").first()
                                                val timestamp = System.currentTimeMillis()
                                                val filename = "$timestamp.$mime"
                                                OriginalImageRetreiver(filename).execute(photo.url)
                                            }

                                            if (result != source) source.recycle()

                                            return result
                                        }
                                    })
                                    .into(imageView, object : Callback {
                                        override fun onSuccess() {
                                            viewSwitcher.showNext()
                                        }

                                        override fun onError() { }
                                    })
                            horizontalLayout.addView(viewSwitcher)
                            if (index % 3 == 2 || index == lastIndex) {
                                verticalLayout.addView(horizontalLayout)
                                horizontalLayout = LinearLayout(this@MyPageActivity)
                                horizontalLayout.layoutMode = LinearLayout.HORIZONTAL
                            }
                        }
                        progressDialog.dismiss()
                    }

                    override fun onCancelled(p0: DatabaseError?) {
                        progressDialog.dismiss()
                        // TODO
                    }
                })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            WRITE_EXTERNAL_STORAGE_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    toast("ACCESS_WRITE_EXTERNAL_STORAGE PERMISSION GRANTED")
                else finish()
                return
            }
            else -> {
                // finish()
            }
        }
    }

    inner class OriginalImageRetreiver: AsyncTask<String, Int, Bitmap> {

        private val progressDialog: ProgressDialog
        private val filename: String
        private val handler: Handler

        constructor(filename: String): super() {
            this.filename = filename
            progressDialog = ProgressDialog(this@MyPageActivity)
            progressDialog.isIndeterminate = true
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progressDialog.setMessage("사진 저장 중")
            handler = Handler()
        }

        override fun doInBackground(vararg params: String?): Bitmap {
            handler.post(Runnable { progressDialog.show() })
            var url = params.get(0)
            return Picasso.with(this@MyPageActivity).load(url).get()
        }

        override fun onPostExecute(result: Bitmap?) {

            var directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "PhotoPlace")
            if (!directory.exists()) directory.mkdir()

            var file = File(directory.path + File.separator + filename)
            try {
                file.createNewFile()
                var ostream = FileOutputStream(file)
                var mime = filename.split(".").last()
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
                MediaScanner(this@MyPageActivity, file)
                handler.post(Runnable { progressDialog.dismiss() })
                toast("사진이 저장됐습니다.")
            }
            catch (exception: IOException) {
                toast(exception.toString())
            }
        }
    }
}
