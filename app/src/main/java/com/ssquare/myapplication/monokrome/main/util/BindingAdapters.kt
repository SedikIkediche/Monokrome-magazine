package com.ssquare.myapplication.monokrome.main.util

import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.makeramen.roundedimageview.RoundedImageView


@BindingAdapter("imageUrl")
fun loadImage(view: RoundedImageView, url: String) {
    Glide.with(view.context).load(url)
        .centerCrop()
        .into(view)
}
