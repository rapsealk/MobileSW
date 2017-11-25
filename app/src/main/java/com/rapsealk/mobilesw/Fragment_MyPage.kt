package com.rapsealk.mobilesw

import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rapsealk.mobilesw.adapter.CardAdapter
import com.rapsealk.mobilesw.schema.Photo
import kotlinx.android.synthetic.main.fragment_my_page.*

class Fragment_MyPage : Fragment() {

    private val WRITE_EXTERNAL_STORAGE_CODE: Int = 10001

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseDatabase: FirebaseDatabase? = null
    var _context: Context? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val View = inflater!!.inflate(R.layout.fragment_my_page, container, false)
        _context = container!!.context
        return View
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        if (ContextCompat.checkSelfPermission(_context!!, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(context, "이미지를 저장하기 위해서는 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
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

        // recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val mCardAdapter = CardAdapter(context)
        recyclerView.adapter = mCardAdapter
        recyclerView.layoutMode = LinearLayout.VERTICAL

        val ref = mFirebaseDatabase?.getReference("users")
        // TODO : add a descending element
        ref?.child("$uid/photos")?.orderByChild("timestamp")
                ?.addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot?) {

                        val children = snapshot!!.children.reversed()
                        val lastIndex = children.count() - 1

                        // var horizontalLayout = LinearLayout(_context)
                        // horizontalLayout.layoutMode = LinearLayout.HORIZONTAL

                        val array = ArrayList<Photo>()

                        children.forEachIndexed { index, child ->
                            val photo = child.getValue<Photo>(Photo::class.java)
                            array.add(photo)
                        }
                        progressDialog.dismiss()
                        mCardAdapter.setList(array)
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
                    Toast.makeText(context, "ACCESS_WRITE_EXTERNAL_STORAGE PERMISSION GRANTED", Toast.LENGTH_SHORT).show()
                // else finish()
                return
            }
            else -> {
                // finish()
            }
        }
    }
}
