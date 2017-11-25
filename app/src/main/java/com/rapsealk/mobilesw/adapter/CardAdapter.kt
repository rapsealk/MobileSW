package com.rapsealk.mobilesw.adapter

import android.content.Context
import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.rapsealk.mobilesw.R
import com.rapsealk.mobilesw.schema.Photo
import com.rapsealk.mobilesw.util.ImageDownloader
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import java.sql.Timestamp
import java.text.SimpleDateFormat

/**
 * Created by rapsealk on 2017. 11. 25..
 */
class CardAdapter : RecyclerView.Adapter<CardAdapter.Companion.CardViewHolder> {

    companion object {
        class CardViewHolder : RecyclerView.ViewHolder {

            var progressBar: ProgressBar
            var imageView: ImageView
            var textViewContent: TextView
            var textViewTimestamp: TextView

            constructor(view: View) : super(view) {
                progressBar = view.findViewById(R.id.progressBar) as ProgressBar
                imageView = view.findViewById(R.id.imageView) as ImageView
                textViewContent = view.findViewById(R.id.textViewContent) as TextView
                textViewTimestamp = view.findViewById(R.id.textViewTimestamp) as TextView
            }
        }
    }

    private val context: Context
    private var mDataset: ArrayList<Photo>

    constructor(context: Context) {
        this.context = context
        mDataset = ArrayList<Photo>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_photocard, parent, false)
        val viewHolder: CardViewHolder = CardViewHolder(view)
        return viewHolder
    }

    // replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val item = mDataset.get(position)
        holder.progressBar.isIndeterminate = true
        holder.progressBar.visibility = ProgressBar.VISIBLE
        holder.textViewContent.text = item.content
        holder.textViewTimestamp.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Timestamp(item.timestamp))
        Picasso.with(context).load(item.url)
                .transform(object : Transformation {
                    override fun key(): String = item.url
                    override fun transform(source: Bitmap): Bitmap {
                        var width: Double = source.width.toDouble()
                        var height: Double = source.height.toDouble()
                        if (height > 960) {
                            width = Math.ceil(width * (960 / height)) // Math.ceil(width * (960 / height))
                            height = 960.0
                        }
                        val result = Bitmap.createScaledBitmap(source, width.toInt(), height.toInt(), false)
                        holder.imageView.setOnClickListener { v: View? ->
                            val mime = item.url.split(".").last().split("?").first()
                            val timestamp = System.currentTimeMillis()
                            val filename = "p$timestamp.$mime"
                            ImageDownloader(context, filename).execute(item.url)
                        }
                        if (result != source) source.recycle()
                        return result
                    }
                }).into(holder.imageView, object : Callback {
                    override fun onSuccess() {
                        holder.progressBar.visibility = ProgressBar.GONE
                        holder.imageView.visibility = ImageView.VISIBLE
                    }

                    override fun onError() {
                        holder.progressBar.visibility = ProgressBar.GONE
                        holder.textViewContent.text = "이미지를 불러오지 못했습니다."
                    }
                })
    }

    override fun getItemCount(): Int = mDataset.size

    public fun setList(list: ArrayList<Photo>): Unit {
        mDataset = ArrayList<Photo>()
        mDataset.addAll(list)
        notifyDataSetChanged()
    }
}