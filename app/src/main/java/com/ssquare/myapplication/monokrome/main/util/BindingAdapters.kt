package com.ssquare.myapplication.monokrome.main.util

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.ssquare.myapplication.monokrome.R


@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String) {
    Glide.with(view.context).load(url)
        .placeholder(R.drawable.common_google_signin_btn_icon_dark_normal_background)
        .centerCrop()
        .into(view)
}
