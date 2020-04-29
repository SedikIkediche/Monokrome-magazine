package com.ssquare.myapplication.monokrome.util

import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.ssquare.myapplication.monokrome.R


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
fun downloadOrRead(view: TextView, filePath: String?) {
    view.text =
        if (filePath == NO_FILE) view.context.getString(R.string.download) else view.context.getString(
            R.string.read
        )
}

@BindingAdapter("previewOrDelete")
fun previewOrDelete(view: TextView, filePath: String?) {
    view.text =
        if (filePath == NO_FILE) view.context.getString(R.string.preview) else view.context.getString(
            R.string.delete
        )
}
