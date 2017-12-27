package com.crakac.ofuton.widget

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import com.crakac.ofuton.R

class AppendedImageView(context: Context) : FrameLayout(context) {
    val TAG: String = "AppendedImageView"
    val imageView: ImageView
    private val cancelButton: ImageView
    private var listener: OnAppendedImageClickListener? = null

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.appended_image_view, this, true)
        imageView = findViewById(R.id.thumbnail)
        imageView.setOnClickListener { _ ->
            listener?.onClickThumbnail()
        }
        cancelButton = findViewById(R.id.cancel_icon)
        cancelButton.setOnClickListener { _ ->
            listener?.onClickCancel()
        }
    }

    fun setImageBitmap(bitmap: Bitmap) {
        imageView.setImageBitmap(bitmap)
    }

    fun setOnAppendedImageListener(listener: OnAppendedImageClickListener) {
        this.listener = listener
    }

    interface OnAppendedImageClickListener {
        fun onClickCancel()
        fun onClickThumbnail()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        listener = null
    }
}