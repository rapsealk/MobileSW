package com.rapsealk.mobilesw

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.rapsealk.mobilesw.schema.Photo
import kotlinx.android.synthetic.main.activity_upload_photo.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception
import kotlin.system.measureTimeMillis

class UploadPhotoActivity : AppCompatActivity() {

    private val READ_STORAGE_CODE = 410
    private val ACQUIRE_FROM_GALLERY_CODE = 400

    private var photoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_photo)

        var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        var mFirebaseUser: FirebaseUser? = mFirebaseAuth.currentUser
        if (mFirebaseUser == null) {
            var intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        var mFirebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
        var mFirebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                toast("저장소를 이용하기 위해서는 권한이 필요합니다.")
            }
            ActivityCompat.requestPermissions(this, Array<String>(1) { Manifest.permission.READ_EXTERNAL_STORAGE }, READ_STORAGE_CODE)
        }

        btnRollback.setOnClickListener { v: View? ->
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnCommit.setOnClickListener { v: View? ->

            toast("path: $photoPath , text: " + editTextContent.text)
            var storageRef: StorageReference = mFirebaseStorage.getReference()

            var photoPathSplitted = photoPath?.split("/")
            var imageFileName: String = photoPathSplitted!!.get(photoPathSplitted!!.size-1)
            var imageFileNameSplitted = imageFileName.split(".")
            var imageFileMimeType = imageFileNameSplitted.get(imageFileNameSplitted.size-1)

            // Upload image on Firebase Storage
            imageViewUpload.isDrawingCacheEnabled = true
            imageViewUpload.buildDrawingCache()
            var bitmap: Bitmap = imageViewUpload.drawingCache
            var byteOutputStream: ByteArrayOutputStream = ByteArrayOutputStream()
            when (imageFileMimeType.toLowerCase()) {
                "jpg", "jpeg" -> {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteOutputStream)
                }
                "png" -> {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteOutputStream)
                }
            }
            var byteData: ByteArray = byteOutputStream.toByteArray()

            var uploadTask: UploadTask = storageRef.putBytes(byteData)
            uploadTask.addOnFailureListener { exception: Exception ->
                toast(exception.toString())
            }.addOnCompleteListener { task: Task<UploadTask.TaskSnapshot> ->
                var url: Uri? = task.getResult().downloadUrl
                var uid = mFirebaseUser!!.uid
                var ref = mFirebaseDatabase.getReference()
                var timestamp: Long = System.currentTimeMillis()
                var updateList: List<Photo> = mutableListOf(
                        Photo(editTextContent.text.toString(), 127.0, 37.0, timestamp, url.toString())
                )
                // var photoSet = Photo(editTextContent.text.toString(), 127.0, 37.0, timestamp, url.toString()) as Object
                // updateList.put("/users/$uid/photos/$timestamp", photoSet)
                // updateList.put("/photos/$uid/$timestamp", photoSet)
                // ref.updateChildren(updateList)
                ref.child("/users/$uid/photos/$timestamp").setValue(updateList)
                ref.child("/photos/$uid/$timestamp").setValue(updateList)
            }
        }

        acquirePhotosFromGallery()
    }

    fun acquirePhotosFromGallery() {
        var intent = Intent(Intent.ACTION_PICK)
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.setType("image/*")
        startActivityForResult(Intent.createChooser(intent, "Select Photos"), ACQUIRE_FROM_GALLERY_CODE)
    }

    fun convertUriToPath(photoURI: Uri): String {
        var project: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        var cursor: Cursor = contentResolver.query(photoURI, project, null, null, null)
        var column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
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
        var photoURI = intent!!.data
        photoPath = convertUriToPath(photoURI)
        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(photoPath)
        }
        catch (e: IOException) {
            toast(e.toString())
        }
        var exifOrientation: Int? = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        var exifDegree: Int = exifOrientationToDegrees(exifOrientation)

        var bitmap: Bitmap = BitmapFactory.decodeFile(photoPath)

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
            else -> {
                finish()
            }
        }
    }
}
