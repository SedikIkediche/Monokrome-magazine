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
import com.ssquare.myapplication.monokrome.data.Magazine


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
fun downloadOrRead(view: TextView, magazine: Magazine) {
    view.text =
        if (magazine.fileUri == NO_FILE) view.context.getString(R.string.download) else view.context.getString(
            R.string.read
        )
    //view clickable when progress=-1 (download hasn't started) or progress= 100(download finished)
    //view.isClickable = magazine.downloadProgress == -1 || magazine.downloadProgress == 100
}

@BindingAdapter("previewOrDelete")
fun previewOrDelete(view: TextView, magazine: Magazine) {
    view.text =
        if (magazine.fileUri == NO_FILE) view.context.getString(R.string.preview) else view.context.getString(
            R.string.delete
        )
}

/**
 * to update Ui I am using two parameters downloadProgress and fileUri, here are the cases:
 * if (fileUri == NO_FILE && progress == -1) File not downloading
 * if (fileUri == NO_FILE && progress > -1  && progress < 100) file is downloading
 * if (fileUri != NO_FILE && progress == 100) file finished downloading
 * to learn more look at downloadFileWithPrDownloader() function
 * */
@BindingAdapter("downloadProgress")
fun downloadProgress(view: TextView, magazine: Magazine) {
    view.text = magazine.downloadProgress.toString()
    view.isVisible =
        magazine.downloadProgress > -1 && magazine.downloadProgress < 100 // file is downloading

    //view clickable when progress=-1 (download hasn't started) or progress= 100(download finished)
    view.isClickable =
        magazine.downloadProgress == -1 || magazine.downloadProgress == 100  //button not clickable during download
}
