package com.ssquare.myapplication.monokrome.util

import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.makeramen.roundedimageview.RoundedImageView
import com.ssquare.myapplication.monokrome.R


@BindingAdapter("imageUrl")
fun loadImage(view: RoundedImageView, url: String) {
    Glide.with(view.context).load(url)
        .placeholder(ColorDrawable(ContextCompat.getColor(view.context,R.color.image_place_holder_color)))
        .transition(GenericTransitionOptions.with(R.anim.fade_in))
        .centerCrop()
        .into(view)


}
