package com.ssquare.myapplication.monokrome.main.util

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
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
