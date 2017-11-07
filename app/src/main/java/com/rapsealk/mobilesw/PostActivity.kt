package com.rapsealk.mobilesw

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.rapsealk.mobilesw.adapter.CommentAdapter
import com.rapsealk.mobilesw.retrofit.CloudMessageService
import com.rapsealk.mobilesw.retrofit.SendingMessage
import com.rapsealk.mobilesw.retrofit.UserService
import com.rapsealk.mobilesw.schema.Comment
import com.rapsealk.mobilesw.schema.Photo
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_post.*
import java.lang.Exception
import java.sql.Timestamp
import java.text.SimpleDateFormat

class PostActivity : AppCompatActivity() {

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseDatabase: FirebaseDatabase? = null
    private var mFirebaseStorage: FirebaseStorage? = null

    private var mCommentAdapter: CommentAdapter? = null
    private var mCommentCount: Long = 0
    private var mLikeCount: Long = 0
    private var mIsLiked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mFirebaseStorage = FirebaseStorage.getInstance()

        val currentUser = mFirebaseAuth?.currentUser
        val uid = currentUser!!.uid

        val serializedData = intent.getSerializableExtra("SerializedData") as Photo
        val postTimestamp = serializedData.timestamp

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        commentListView.layoutParams.height = metrics.heightPixels - (cardView.height + linearLayout.height)

        writerId.text = serializedData.uid
        writtenTime.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Timestamp(postTimestamp))
        content.text = serializedData.content

        optionalButton.setOnClickListener { v: View? ->
            if (serializedData.uid != uid) return@setOnClickListener
            val dialogBuilder = AlertDialog.Builder(this@PostActivity)
                    .setTitle("사진을 삭제하시겠습니까?")
                    .setMessage("삭제된 사진은 복구되지 않습니다.")
                    .setPositiveButton("삭제", DialogInterface.OnClickListener { dialog: DialogInterface, which: Int ->
                        /*
                        val progressDialog = ProgressDialog(this)
                        progressDialog.isIndeterminate = true
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                        progressDialog.setMessage("내 사진들 불러오는 중")
                        progressDialog.show()
                        */
                        val url = serializedData.url
                        val imageName = url.split("?").get(0).split("/").last()
                        Log.d("URL", url);
                        Log.d("ImageName", imageName)
                        // TODO
                        // toast("imageName: $imageName")
                        mFirebaseStorage?.getReference("$uid/$imageName")?.delete()
                        mFirebaseDatabase?.getReference("photos/$postTimestamp")?.removeValue()
                        mFirebaseDatabase?.getReference("users/$uid/photos/$postTimestamp")?.removeValue()
                        val intent = Intent()
                        intent.putExtra("id", postTimestamp.toString())
                        setResult(RESULT_OK, intent)
                        finish()
                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, which ->
                        // TODO("Not required")
                    }).create()
            dialogBuilder.show()
        }

        Picasso.with(this)
                .load("http://52.78.4.96:3003/images/"+serializedData.uid)
                .transform(object : Transformation {
                    override fun key(): String = ""
                    override fun transform(source: Bitmap?): Bitmap {
                        val size = Math.min(source!!.height, source.width)
                        val x = (source.width - size) / 2
                        val y = (source.height - size) / 2
                        val square = Bitmap.createBitmap(source, x, y, size, size)
                        if (square != source) source.recycle()
                        val bitmap = Bitmap.createBitmap(size, size, source.config)
                        val canvas = Canvas(bitmap)
                        val paint = Paint()
                        val shader = BitmapShader(square, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                        paint.shader = shader
                        paint.isAntiAlias = true
                        val round = size / 2f
                        canvas.drawCircle(round, round, round, paint)
                        square.recycle()
                        return bitmap
                    }
                })
                .into(profileImage)

        val userService = UserService.create()
        userService.getUser(serializedData.uid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    Log.d("RxLog", result.toString())
                    writerId.text = result.data.displayName
                }, { error ->
                    error.printStackTrace()
                })

        Picasso.with(this)
                .load(serializedData.url)
                .transform(object : Transformation {
                    override fun key(): String = ""
                    override fun transform(source: Bitmap?): Bitmap {
                        val maxHeight = 960
                        val isSuperBig = source!!.height > maxHeight
                        val x = source.width // if (isSuperBig) (source.width / source.height).toInt() * 480 else source.width
                        val y = if (isSuperBig) maxHeight else source.height
                        val image = Bitmap.createScaledBitmap(source, x, y, false)
                        if (image != source) source.recycle()
                        return image
                    }
                })
                .into(imageViewPost as ImageView)

        mFirebaseDatabase?.getReference("photos/$postTimestamp/comments")
                ?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot?) {

                        mCommentCount = snapshot!!.childrenCount

                        val comments: ArrayList<Comment> = arrayListOf()

                        for (value in snapshot.children) {
                            val comment = value.getValue<Comment>(Comment::class.java)
                            comments.add(comment)

                            userService.getUser(comment.uid)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe({ result ->
                                        Log.d("RxLog", result.toString())
                                        comment.displayName = result.data.displayName
                                        mCommentAdapter?.notifyDataSetChanged()
                                    }, { error ->
                                        error.printStackTrace()
                                    })
                        }

                        mCommentAdapter = CommentAdapter(this@PostActivity, comments)
                        commentListView.adapter = mCommentAdapter

                        commentListView.setOnItemClickListener { parent, view, position, id ->
                            val comment = comments[position]
                            if (serializedData.uid == uid || comment.uid == uid) {
                                val commentTimestamp = comment.timestamp
                                val dialogBuilder = AlertDialog.Builder(this@PostActivity)
                                        .setTitle("댓글을 삭제하시겠습니까?")
                                        .setMessage("삭제된 댓글은 복구되지 않습니다.")
                                        .setPositiveButton("삭제", DialogInterface.OnClickListener { dialog: DialogInterface, which: Int ->
                                            mFirebaseDatabase?.getReference("photos/$postTimestamp/comments/$commentTimestamp")
                                                    ?.removeValue()
                                            comments.removeAt(position)
                                            mCommentAdapter?.notifyDataSetChanged()
                                            updateCommentCount()
                                        })
                                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, which ->
                                            // TODO("Not required")
                                        }).create()
                                dialogBuilder.show()
                            }
                        }

                        updateCommentCount()
                    }

                    override fun onCancelled(p0: DatabaseError?) {
                        toast("댓글 불러오기에 실패했습니다.")
                    }
                })

        mFirebaseDatabase?.getReference("photos/$postTimestamp/phopls")
                ?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot?) {

                        mLikeCount = snapshot!!.childrenCount
                        updateLikes(mLikeCount)

                        for (phopl in snapshot.children) {
                            if (phopl.key == uid) {
                                mIsLiked = true
                                btnPhopl.setImageResource(R.drawable.star_yellow)
                            }
                        }
                    }

                    override fun onCancelled(p0: DatabaseError?) {
                        toast("포플 불러오기에 실패했습니다.")
                    }
                })

        editTextComment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                btnPostComment.isEnabled = if (s.toString().trim().length > 0) true else false
            }
            override fun afterTextChanged(s: Editable?) { }
        })

        btnPostComment.isEnabled = false

        btnPostComment.setOnClickListener { v: View ->
            val comment = editTextComment.text.toString()
            // if (comment == "") return@setOnClickListener
            val commentTimestamp = System.currentTimeMillis()
            val Comment = Comment(comment, commentTimestamp, uid)
            val ref = mFirebaseDatabase?.reference
            ref?.child("photos/$postTimestamp/comments/$commentTimestamp")?.setValue(Comment)
                    ?.addOnCompleteListener { task: Task<Void> ->
                        Comment.displayName = currentUser.displayName
                        mCommentAdapter?.comments?.add(Comment)
                        mCommentAdapter?.notifyDataSetChanged()
                        editTextComment.setText("")
                        updateCommentCount()

                        // FCM REQUEST
                        if (serializedData.uid != uid) {
                            val cmService = CloudMessageService.create()
                            cmService.sendMessage(SendingMessage(currentUser.displayName!!, serializedData.uid, postTimestamp, comment))
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe({ result ->
                                        Log.d("RxLog", result.toString())
                                    }, { error ->
                                        error.printStackTrace()
                                    })
                        }

                    }
                    ?.addOnFailureListener { exception: Exception ->
                        toast("댓글 달기에 실패했습니다.")
                    }
        }

        btnPhopl.setOnClickListener { v: View ->
            val ref = mFirebaseDatabase?.getReference("photos/$postTimestamp/phopls/$uid")
            val phoplTask = if (mIsLiked) ref?.removeValue() else ref?.setValue(System.currentTimeMillis())
            phoplTask
                    ?.addOnCompleteListener { task: Task<Void> ->
                        mIsLiked = !mIsLiked
                        btnPhopl.setImageResource( if (mIsLiked) R.drawable.star_yellow else R.drawable.star_gray )
                        updateLikes( if (mIsLiked) ++mLikeCount else --mLikeCount )
                    }
                    ?.addOnFailureListener { exception: Exception ->
                        toast("다시 시도해보세요.")
                    }
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("id", "null")
        setResult(RESULT_OK, intent)
        finish()
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun updateLikes(count: Long) {
        commentPhopl.text = "포플 ($count)"
    }

    fun updateCommentCount() {
        val count = mCommentAdapter?.count
        commentInfo.text ="댓글 ($count)"
    }
}
