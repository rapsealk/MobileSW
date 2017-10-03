package com.rapsealk.mobilesw.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.rapsealk.mobilesw.R
import com.rapsealk.mobilesw.schema.Comment
import kotlinx.android.synthetic.main.list_item_comment.view.*
import java.sql.Timestamp
import java.text.SimpleDateFormat

/**
 * Created by rapsealk on 2017. 10. 3..
 */
class CommentAdapter(context: Context, val comments: ArrayList<Comment>) : BaseAdapter() {

    val mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int = comments.size

    override fun getItem(position: Int): Comment = comments.get(position)

    override fun getItemId(position: Int): Long = position.toLong() // comments.get(position)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var comment = getItem(position)
        var rowView = mInflater.inflate(R.layout.list_item_comment, parent, false)
        rowView.commentWriter.text = comment.uid
        rowView.commentContent.text = comment.comment
        rowView.commentTimestamp.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(Timestamp(comment.timestamp))
        return rowView
    }

}