package com.rapsealk.mobilesw

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rapsealk.mobilesw.schema.Photo
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import kotlinx.android.synthetic.main.activity_my_page.*

class MyPageActivity : AppCompatActivity() {

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseDatabase: FirebaseDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        var progressDialog = ProgressDialog(this)
        progressDialog.isIndeterminate = true
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setMessage("내 사진들 불러오는 중")

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        var user = mFirebaseAuth?.currentUser
        var uid = user?.uid

        btnBack.setOnClickListener { v: View? ->
            finish()
        }

        progressDialog.show()
        var ref = mFirebaseDatabase?.getReference("users")
        ref?.child("$uid/photos")
                ?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot?) {

                        var children = snapshot!!.children
                        var lastIndex = children.count() - 1

                        var targetWidth = (verticalLayout.width / 3).toInt()

                        var horizontalLayout = LinearLayout(this@MyPageActivity)
                        horizontalLayout.layoutMode = LinearLayout.HORIZONTAL

                        snapshot!!.children.forEachIndexed { index, child ->
                            var photo = child.getValue<Photo>(Photo::class.java)
                            var imageView = ImageView(this@MyPageActivity)
                            Picasso.with(this@MyPageActivity)
                                    .load(photo.url)
                                    .transform(object: Transformation {
                                        override fun key(): String = ""
                                        override fun transform(source: Bitmap): Bitmap {
                                            var ratio = source.width / source.height
                                            var targetHeight = (targetWidth * ratio).toInt()
                                            var result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false)
                                            if (result != source) source.recycle()
                                            return result
                                        }
                                    })
                                    .into(imageView) //, object : Callback {})
                            horizontalLayout.addView(imageView)
                            if (index % 3 == 2 || index == lastIndex) {
                                verticalLayout.addView(horizontalLayout)
                                horizontalLayout = LinearLayout(this@MyPageActivity)
                                horizontalLayout.layoutMode = LinearLayout.HORIZONTAL
                            }

                            imageView.setOnClickListener { v: View? ->
                                toast("url: " + photo.url)
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
}
