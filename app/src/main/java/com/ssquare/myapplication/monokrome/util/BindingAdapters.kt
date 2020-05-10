package com.ssquare.myapplication.monokrome.util

import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.data.DownloadState
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.data.getDownloadState


@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String) {
    Glide.with(view.context).load(url)
        .placeholder(
            ColorDrawable(
                ContextCompat.getColor(
                    view.context,
                    R.color.image_place_holder_color
                )
            )
        )
        .transition(GenericTransitionOptions.with(R.anim.fade_in))
        .centerCrop()
        .into(view)
}

@BindingAdapter("downloadOrRead")
fun downloadOrCancelOrRead(view: TextView, magazine: Magazine) {
    view.text = when (magazine.getDownloadState()) {
        DownloadState.RUNNING, DownloadState.PENDING, DownloadState.PAUSED -> {
            view.context.getString(android.R.string.cancel)
        }
        DownloadState.EMPTY -> {
            view.context.getString(R.string.download)
        }

        DownloadState.COMPLETED -> {
            view.context.getString(R.string.read)
        }

    }

}

@BindingAdapter("previewOrDelete")
fun previewOrDelete(view: TextView, magazine: Magazine) {
    view.text =
        if (magazine.getDownloadState() == DownloadState.COMPLETED) view.context.getString(R.string.delete) else view.context.getString(
            R.string.preview
        )
}


@BindingAdapter("downloadState")
fun downloadState(view: TextView, magazine: Magazine) {
    view.text = DownloadState.values()[magazine.downloadState].toString()
    view.isVisible = when (magazine.getDownloadState()) {
        DownloadState.PENDING, DownloadState.RUNNING, DownloadState.PAUSED -> true
        else -> false
    }
    // file is downloading or paused
}

@BindingAdapter("downloadProgress")
fun downloadProgress(view: TextView, magazine: Magazine) {
    view.text = magazine.downloadProgress.toString()
    view.isVisible = magazine.getDownloadState() == DownloadState.RUNNING// file is downloading
}
