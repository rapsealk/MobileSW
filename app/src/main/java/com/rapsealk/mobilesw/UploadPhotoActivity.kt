package com.rapsealk.mobilesw

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.rapsealk.mobilesw.util.SharedPreferenceManager
import com.rapsealk.mobilesw.schema.Comment
import com.rapsealk.mobilesw.schema.Photo
import kotlinx.android.synthetic.main.activity_upload_photo.*
import java.io.File
import java.io.IOException
import java.lang.Exception

class UploadPhotoActivity : AppCompatActivity() {

    private val FINE_LOCATION_CODE = 1
    private val ACQUIRE_FROM_GALLERY_CODE = 400
    private val READ_STORAGE_CODE = 410

    private var mLocationManager: LocationManager? = null
    private var mSharedPreference: SharedPreferenceManager? = null

    private var photoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_photo)

        val mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val mFirebaseUser: FirebaseUser? = mFirebaseAuth.currentUser
        if (mFirebaseUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        val mFirebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
        val mFirebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        mSharedPreference = SharedPreferenceManager.getInstance(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                toast("저장소를 이용하기 위해서는 권한이 필요합니다.")
            }
            ActivityCompat.requestPermissions(this, Array<String>(1) { Manifest.permission.READ_EXTERNAL_STORAGE }, READ_STORAGE_CODE)
        }

        imageViewUpload.setOnClickListener { v: View? ->
            acquirePhotosFromGallery()
        }

        btnRollback.setOnClickListener { v: View? ->
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnCommit.setOnClickListener { v: View? ->

            val progressDialog = ProgressDialog(this)
            progressDialog.isIndeterminate = true
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progressDialog.setMessage("이미지 업로드 중...")
            progressDialog.show()

            val storageRef: StorageReference = mFirebaseStorage.getReference()

            val uid = mFirebaseUser!!.uid
            val content = editTextContent.text.toString()

            val location = mSharedPreference!!.getLastKnownLocation() ?: LatLng(127.0, 37.0)
            val latitude = location.latitude
            val longitude = location.longitude

            val file = Uri.fromFile(File(photoPath))
            val timestamp = System.currentTimeMillis()
            val imageFileName = file.lastPathSegment
            val uploadTask = storageRef.child("$uid/$imageFileName").putFile(file)

            uploadTask
                    .addOnFailureListener { exception: Exception -> toast(exception.toString()) }
                    .addOnCompleteListener { task: Task<UploadTask.TaskSnapshot> ->
                        val url = task.result.downloadUrl.toString()
                        val ref = mFirebaseDatabase.getReference()
                        val photoData = Photo(HashMap<String, Comment>(), content, latitude, longitude, HashMap<String, Long>(), timestamp, uid, url)
                        ref.child("users/$uid/photos/$timestamp").setValue(photoData)
                        ref.child("photos/$timestamp").setValue(photoData)
                        progressDialog.dismiss()
                        toast("Upload succeed.")
                        btnRollback.performClick()
                    }
        }

        acquirePhotosFromGallery()
    }

    fun acquirePhotosFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.setType("image/*")
        startActivityForResult(Intent.createChooser(intent, "Select Photos"), ACQUIRE_FROM_GALLERY_CODE)
    }

    fun convertUriToPath(photoURI: Uri): String {
        val project: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor = contentResolver.query(photoURI, project, null, null, null)
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }

    fun exifOrientationToDegrees(exifOrientation: Int?): Int {
        when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> return 90
            ExifInterface.ORIENTATION_ROTATE_180 -> return 180
            ExifInterface.ORIENTATION_ROTATE_270 -> return 270
            else -> return 0
        }
    }

    fun loadPhotosFromURI(intent: Intent?) {
        val photoURI = intent!!.data
        photoPath = convertUriToPath(photoURI)
        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(photoPath)
        }
        catch (e: IOException) {
            toast(e.toString())
        }
        val exifOrientation: Int? = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        var exifDegree: Int = exifOrientationToDegrees(exifOrientation)

        val bitmap: Bitmap = BitmapFactory.decodeFile(photoPath)

        imageViewUpload.setImageBitmap(bitmap)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                ACQUIRE_FROM_GALLERY_CODE -> {
                    loadPhotosFromURI(data)
                    return
                }
                else -> {
                    finish()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            READ_STORAGE_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    toast("ACCESS_READ_STORAGE PERMISSION GRANTED")
                else finish()
                return
            }
            FINE_LOCATION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    toast("ACCESS_FINE_LOCATION PERMISSION GRANTED")
                else finish()
                return
            }
            else -> {
                finish()
            }
        }
    }
}
