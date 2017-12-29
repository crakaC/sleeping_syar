package com.crakac.ofuton.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.crakac.ofuton.R

class AppendedImageView(context: Context) : FrameLayout(context) {
    val TAG: String = "AppendedImageView"
    val imageView: ImageView
    private val cancelButton: ImageView
    private var listener: OnAppendedImageClickListener? = null
    private val progress: View

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
        progress = findViewById(R.id.progress)
    }

    fun setOnAppendedImageListener(listener: OnAppendedImageClickListener) {
        this.listener = listener
    }

    fun clearProgress(){
        progress.visibility = View.GONE
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