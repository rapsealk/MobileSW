package com.rapsealk.mobilesw

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.rapsealk.mobilesw.schema.Comment
import com.rapsealk.mobilesw.schema.Photo
import com.rapsealk.mobilesw.util.SharedPreferenceManager
import com.rapsealk.mobilesw.util.ThumbImageInfo
import java.io.File
import java.util.*

class SelectPicture : AppCompatActivity() {

    internal var mBusy = false
    internal var mLoagindDialog: ProgressDialog? = null
    internal lateinit var mGvImageList: GridView
    internal lateinit var mListAdapter: ImageAdapter
    internal lateinit var mThumbImageInfoList: ArrayList<ThumbImageInfo>
    internal lateinit var btnOK: Button
    internal lateinit var btnCancel: Button
    internal var time: Long = 0
    private var mSharedPreference: SharedPreferenceManager? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_picture)

        var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        var mFirebaseUser: FirebaseUser? = mFirebaseAuth.currentUser
        if (mFirebaseUser == null) {
            var intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        var mFirebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
        var mFirebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
        mSharedPreference = SharedPreferenceManager.getInstance(this)

        val intent = intent
        time = intent.getLongExtra("time", 0)
        mThumbImageInfoList = ArrayList()
        mGvImageList = findViewById(R.id.gvImageList) as GridView
        //mGvImageList.setOnScrollListener(this);
        //mGvImageList.setOnItemClickListener(this);
        btnOK = findViewById(R.id.btnSelectOk) as Button
        btnCancel = findViewById(R.id.btnSelectCancel) as Button

        // new DoFindImageList().execute();

        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA)
        // 쿼리 수행
        val imageCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                MediaStore.Images.Media.DATE_TAKEN + " >=" + time, null, MediaStore.Images.Media.DATE_ADDED + " desc ")

        if (imageCursor != null && imageCursor.count > 0) {
            val imageIDCol = imageCursor.getColumnIndex(MediaStore.Images.Media._ID)
            val imageDataCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA)

            while (imageCursor.moveToNext()) {
                val thumbInfo = ThumbImageInfo()

                thumbInfo.id = imageCursor.getString(imageIDCol)
                thumbInfo.data = imageCursor.getString(imageDataCol)
                thumbInfo.checkedState = false

                mThumbImageInfoList.add(thumbInfo)
            }
        }
        imageCursor!!.close()

        mListAdapter = ImageAdapter(this, R.layout.image_cell, mThumbImageInfoList)
        mGvImageList.adapter = mListAdapter



        btnOK.setOnClickListener {
            val temp = ArrayList<ThumbImageInfo>()
            for (i in mThumbImageInfoList.indices) {
                if (mThumbImageInfoList[i].checkedState) {
                    temp.add(mThumbImageInfoList[i])
                }
            }
            if(temp.size ==0){
                toast("사진을 선택해주세요")
            }else {
                val progressDialog = ProgressDialog(this)
                progressDialog.isIndeterminate = true
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                progressDialog.setMessage("이미지 업로드 중...")
                progressDialog.show()


                val storageRef: StorageReference = mFirebaseStorage.getReference()

                val uid = mFirebaseUser!!.uid
                val content = System.currentTimeMillis().toString() // "//"

                val location = mSharedPreference!!.getLastKnownLocation() ?: LatLng(127.0, 37.0)
                val latitude = location.latitude
                val longitude = location.longitude


                for (i in temp.indices) {
                    // var file = Uri.fromFile(File(temp[i].data))
                    val file = File(temp[i].data)
                    //var timestamp = System.currentTimeMillis()///////////////////////////////////이부분 고치기
                    val timestamp = file.lastModified()
                    val imageFileName = file.name

                    val uploadTask = storageRef.child("$uid/$imageFileName").putFile(Uri.fromFile(File(temp[i].data)))


                    uploadTask
                            .addOnFailureListener { exception: java.lang.Exception -> toast(exception.toString()) }
                            .addOnCompleteListener { task: Task<UploadTask.TaskSnapshot> ->
                                val url = task.result.downloadUrl.toString()
                                val ref = mFirebaseDatabase.getReference()
                                val photoData = Photo(HashMap<String, Comment>(), content, latitude, longitude, HashMap<String, Long>(), timestamp, uid, url)
                                ref.child("users/$uid/photos/$timestamp").setValue(photoData)
                                ref.child("photos/$timestamp").setValue(photoData)
                                //  btnRollback.performClick()
                            }

                }
                progressDialog.dismiss()
                toast("Upload succeed.")
                finish()
            }
        }

        btnCancel.setOnClickListener { finish() }

        mGvImageList.onItemClickListener = AdapterView.OnItemClickListener { arg0, arg1, position, arg3 ->
            val adapter = arg0.adapter as ImageAdapter
            val rowData = adapter.getItem(position) as ThumbImageInfo
            val curCheckState = rowData.checkedState

            rowData.checkedState = !curCheckState

            mThumbImageInfoList[position] = rowData
            adapter.notifyDataSetChanged()
        }
    }

    // ***************************************************************************************** //
    // Image Adapter Class
    // ***************************************************************************************** //
    internal class ImageViewHolder {
        var ivImage: ImageView? = null
        var chkImage: CheckBox? = null
    }

    public inner class ImageAdapter(private val mContext: Context, private val mCellLayout: Int, private val mThumbImageInfoList: ArrayList<ThumbImageInfo>) : BaseAdapter() {
        private val mLiInflater: LayoutInflater

        init {

            mLiInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }

        override fun getCount(): Int {
            return mThumbImageInfoList.size
        }

        override fun getItem(position: Int): Any {
            return mThumbImageInfoList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            if (convertView == null) {
                convertView = mLiInflater.inflate(mCellLayout, parent, false)
                val holder = ImageViewHolder()

                holder.ivImage = convertView!!.findViewById(R.id.ivImage) as ImageView
                holder.chkImage = convertView.findViewById(R.id.chkImage) as CheckBox

                convertView.tag = holder
            }

            val holder = convertView.tag as ImageViewHolder

            if (mThumbImageInfoList[position].checkedState)
                holder.chkImage!!.isChecked = true
            else
                holder.chkImage!!.isChecked = false

            try {
                val path = mThumbImageInfoList[position].data

                val option = BitmapFactory.Options()

                if (File(path!!).length() > 100000)
                    option.inSampleSize = 10
                else
                    option.inSampleSize = 2

                val bmp = BitmapFactory.decodeFile(path, option)
                holder.ivImage!!.setImageBitmap(bmp)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return convertView
        }
    }
}
