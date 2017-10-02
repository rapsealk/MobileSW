package com.rapsealk.mobilesw

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

class PostActivity : AppCompatActivity() {

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseDatabase: FirebaseDatabase? = null
    // private var serializedData: Photo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseDatabase = FirebaseDatabase.getInstance()

        var currentUser = mFirebaseAuth?.currentUser
        var uid = currentUser!!.uid

        var serializedData = intent.getSerializableExtra("SerializedData") as Photo
        var postTimestamp = serializedData.timestamp

        Picasso.with(this)
                .load(serializedData?.url)
                .into(imageViewPost as ImageView)

        mFirebaseDatabase?.getReference("photos/$postTimestamp/comments")
                ?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot?) {
                        for (value in snapshot!!.children) {
                            var comment = value.getValue<Comment>(Comment::class.java)
                            var oldComment = TextView(this@PostActivity)
                            var writer = comment.uid
                            var timestamp = comment.timestamp
                            var text = comment.comment
                            oldComment.text = "$writer: $text ($timestamp)"
                            commentLayout.addView(oldComment)
                        }
                    }

                    override fun onCancelled(p0: DatabaseError?) {
                        toast("댓글 불러오기에 실패했습니다.")
                    }
                })


        btnPostComment.setOnClickListener { v: View ->
            var comment = editTextComment.text.toString()
            var commentTimestamp = System.currentTimeMillis()
            var Comment = Comment(comment, commentTimestamp, uid)
            var ref = mFirebaseDatabase?.getReference()
            ref?.child("photos/$postTimestamp/comments/$commentTimestamp")?.setValue(Comment)
                    ?.addOnCompleteListener { task: Task<Void> ->
                        var newComment = TextView(this)
                        newComment.text = "$uid: $comment ($commentTimestamp)"
                        commentLayout.addView(newComment)
                        editTextComment.setText("")
                    }
                    ?.addOnFailureListener { exception: Exception ->
                        toast("댓글 달기에 실패했습니다.")
                    }
        }
    }

    override fun onBackPressed() {
        // finish()
        super.onBackPressed()
    }
}
