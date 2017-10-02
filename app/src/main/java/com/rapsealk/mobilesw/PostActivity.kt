package com.rapsealk.mobilesw

import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rapsealk.mobilesw.schema.Comment
import com.rapsealk.mobilesw.schema.Photo
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_post.*
import java.lang.Exception
import java.sql.Timestamp
import java.text.SimpleDateFormat

class PostActivity : AppCompatActivity() {

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseDatabase: FirebaseDatabase? = null
    // private var serializedData: Photo? = null

    private var commentCount: Long = 0
    private var phoplCount: Long = 0
    private var isPhoPled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseDatabase = FirebaseDatabase.getInstance()

        var currentUser = mFirebaseAuth?.currentUser
        var uid = currentUser!!.uid

        var serializedData = intent.getSerializableExtra("SerializedData") as Photo
        var postTimestamp = serializedData.timestamp

        /*
        var phopls = serializedData.phopls
        var phoplTimestamp = phopls.get(uid)
        // phopls.plus(Pair<String, Long>(uid, System.currentTimeMillis()))
        if (phoplTimestamp != null) {
            isPhoPled = true
            btnPhoPl.setImageResource(R.drawable.star_yellow)
        }
        */

        Picasso.with(this)
                .load(serializedData?.url)
                .into(imageViewPost as ImageView)

        mFirebaseDatabase?.getReference("photos/$postTimestamp/comments")
                ?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot?) {

                        commentCount = snapshot!!.childrenCount
                        updateCommentCount(commentCount)

                        for (value in snapshot!!.children) {
                            var comment = value.getValue<Comment>(Comment::class.java)
                            var oldComment = TextView(this@PostActivity)
                            var writer = comment.uid
                            var timestamp = SimpleDateFormat("yyyy-MM-dd H:m:s:S").format(Timestamp(comment.timestamp))
                            var text = comment.comment
                            oldComment.text = "$writer: $text ($timestamp)"
                            commentLayout.addView(oldComment)
                        }
                    }

                    override fun onCancelled(p0: DatabaseError?) {
                        toast("댓글 불러오기에 실패했습니다.")
                    }
                })

        mFirebaseDatabase?.getReference("photos/$postTimestamp/phopls")
                ?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot?) {

                        phoplCount = snapshot!!.childrenCount
                        updatePhoplCount(phoplCount)

                        for (phopl in snapshot!!.children) {
                            if (phopl.key == uid) {
                                isPhoPled = true
                                btnPhoPl.setImageResource(R.drawable.star_yellow)
                            }
                        }
                    }

                    override fun onCancelled(p0: DatabaseError?) {
                        toast("포플 불러오기에 실패했습니다.")
                    }
                })

        btnPostComment.setOnClickListener { v: View ->
            var comment = editTextComment.text.toString()
            var commentTimestamp = System.currentTimeMillis()
            var Comment = Comment(comment, commentTimestamp, uid)
            var ref = mFirebaseDatabase?.reference
            ref?.child("photos/$postTimestamp/comments/$commentTimestamp")?.setValue(Comment)
                    ?.addOnCompleteListener { task: Task<Void> ->
                        var newComment = TextView(this)
                        var formatstamp = SimpleDateFormat("yyyy-MM-dd H:m:s:S").format(Timestamp(commentTimestamp))
                        newComment.text = "$uid: $comment ($formatstamp)"
                        commentLayout.addView(newComment)
                        editTextComment.setText("")
                        updateCommentCount(++commentCount)
                    }
                    ?.addOnFailureListener { exception: Exception ->
                        toast("댓글 달기에 실패했습니다.")
                    }
        }

        btnPhoPl.setOnClickListener { v: View ->
            var ref = mFirebaseDatabase?.getReference("photos/$postTimestamp/phopls/$uid")
            var phoplTask = if (isPhoPled) ref?.removeValue() else ref?.setValue(System.currentTimeMillis())
            phoplTask
                    ?.addOnCompleteListener { task: Task<Void> ->
                        isPhoPled = !isPhoPled
                        btnPhoPl.setImageResource( if (isPhoPled) R.drawable.star_yellow else R.drawable.star_gray )
                        updatePhoplCount( if (isPhoPled) ++phoplCount else --phoplCount )
                    }
                    ?.addOnFailureListener { exception: Exception ->
                        toast("다시 시도해보세요.")
                    }
        }
    }

    override fun onBackPressed() {
        // finish()
        super.onBackPressed()
    }

    fun updatePhoplCount(count: Long) {
        commentPhoPl.text = "포플 ($count)"
    }

    fun updateCommentCount(count: Long) {
        commentInfo.text = "댓글 ($count)"
    }
}
