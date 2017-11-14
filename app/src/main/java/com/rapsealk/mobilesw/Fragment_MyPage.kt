package com.rapsealk.mobilesw

import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ViewSwitcher
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rapsealk.mobilesw.schema.Photo
import com.rapsealk.mobilesw.util.ImageDownloader
import com.rapsealk.mobilesw.util.MediaScanner
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import kotlinx.android.synthetic.main.fragment_my_page.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Fragment_MyPage : Fragment() {

    private val WRITE_EXTERNAL_STORAGE_CODE: Int = 10001

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseDatabase: FirebaseDatabase? = null
    var _context: Context? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val View =inflater!!.inflate(R.layout.fragment_my_page, container, false)
        _context = container!!.context
        return View
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_my_page)

        if (ContextCompat.checkSelfPermission(_context!!, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //toast("이미지를 저장하기 위해서는 권한이 필요합니다.")
            }
            ActivityCompat.requestPermissions(activity, Array<String>(1) { android.Manifest.permission.WRITE_EXTERNAL_STORAGE }, WRITE_EXTERNAL_STORAGE_CODE)
        }

        val progressDialog = ProgressDialog(_context)
        progressDialog.isIndeterminate = true
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setMessage("내 사진들 불러오는 중")

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        val user = mFirebaseAuth?.currentUser
        val uid = user?.uid

        progressDialog.show()

        val ref = mFirebaseDatabase?.getReference("users")
        // TODO : add a descending element
        ref?.child("$uid/photos")?.orderByChild("timestamp")
                ?.addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot?) {

                        val children = snapshot!!.children.reversed()
                        val lastIndex = children.count() - 1

                        var horizontalLayout = LinearLayout(_context)
                        horizontalLayout.layoutMode = LinearLayout.HORIZONTAL
                       //
                        children.forEachIndexed { index, child ->
                            val photo = child.getValue<Photo>(Photo::class.java)
                            val viewSwitcher = ViewSwitcher(_context)
                            val progressBar = ProgressBar(_context)
                            progressBar.isIndeterminate = true
                            progressBar.visibility = ProgressBar.VISIBLE
                            viewSwitcher.addView(progressBar)
                            val imageView = ImageView(_context)
                            viewSwitcher.addView(imageView)
                            Picasso.with(_context)
                                    .load(photo.url)
                                    .transform(object: Transformation {
                                            override fun key(): String = ""
                                            override fun transform(source: Bitmap): Bitmap {
                                                val result = Bitmap.createScaledBitmap(source, 480, 640, false)
                                                imageView.setOnClickListener { v: View? ->
                                                    val mime = photo.url.split(".").last().split("?").first()
                                                    val timestamp = System.currentTimeMillis()
                                                    val filename = "$timestamp.$mime"
                                                    ImageDownloader(_context!!, filename).execute(photo.url)
                                                }

                                                if (result != source) source.recycle()

                                                return result
                                            }
                                        }

                                    )
                                    .into(imageView, object : Callback {
                                        override fun onSuccess() {
                                            viewSwitcher.showNext()
                                        }

                                        override fun onError() { }
                                    })

                            horizontalLayout.addView(viewSwitcher)
                            //

                            if (index % 3 == 2 || index == lastIndex) {
                             //   vLayout.addView(getView())
                                vLayout.addView(horizontalLayout)
                                horizontalLayout = LinearLayout(_context)
                                horizontalLayout.layoutMode = LinearLayout.HORIZONTAL
                            }
                            //
                        }
                        progressDialog.dismiss()

                    }

                    override fun onCancelled(p0: DatabaseError?) {
                        progressDialog.dismiss()
                        // TODO
                    }
                })
    }


/*
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            WRITE_EXTERNAL_STORAGE_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
               //     toast("ACCESS_WRITE_EXTERNAL_STORAGE PERMISSION GRANTED")
                else finish()
                return
            }
            else -> {
                // finish()
            }
        }
    }
*/
}
