package com.ssquare.myapplication.monokrome.util

import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.text.format.DateFormat
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import com.makeramen.roundedimageview.RoundedImageView
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.data.DomainMagazine
import com.ssquare.myapplication.monokrome.data.getDownloadState
import java.util.*


@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: GlideUrl?) {
    url?.let {
     val reqopts = RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)
          .override(view.width,view.height)
        Glide.with(view.context)
            .load(url)
            .apply(reqopts)
            .placeholder(ColorDrawable(ContextCompat.getColor(view.context, R.color.image_place_holder_color)))
            .centerCrop()
            .into(view)

    }
}

@BindingAdapter("imageTint")
fun imageTint(view: RoundedImageView,magazine: DomainMagazine?){
    magazine?.let {
        view.setColorFilter(when(magazine.getDownloadState()){
            DownloadState.COMPLETED,DownloadState.EMPTY -> ContextCompat.getColor(view.context,android.R.color.transparent)
            else -> ContextCompat.getColor(view.context,R.color.image_view_tint_color)
        })
    }
}

@BindingAdapter("date")
fun date(view: TextView, timeStamp: Long) {
    val calendar = Calendar.getInstance().apply { timeInMillis = timeStamp * 1000L }
    val date = DateFormat.format("dd MMMM yyyy", calendar).toString()
    view.text = date
}

//for listFragment

@BindingAdapter("downloadOrRead")
fun downloadOrCancelOrRead(view: TextView, magazine: DomainMagazine?) {
    magazine?.let {
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
        view.setTextColor(when(magazine.getDownloadState()){
            DownloadState.RUNNING, DownloadState.PENDING, DownloadState.PAUSED -> ContextCompat.getColor(view.context,android.R.color.holo_red_dark)
            else -> ContextCompat.getColor(view.context,R.color.list_item_buttons_text_color)
        })
    }
}

@BindingAdapter("previewOrDelete")
fun previewOrDelete(view: TextView, magazine: DomainMagazine?) {
    magazine?.let {
        view.text =
            when(magazine.getDownloadState()){
                DownloadState.COMPLETED -> view.context.getString(R.string.delete)
                else -> view.context.getString(R.string.preview)
            }
        view.setTextColor(when(magazine.getDownloadState()){
            DownloadState.COMPLETED -> ContextCompat.getColor(view.context,android.R.color.holo_red_dark)
            else -> ContextCompat.getColor(view.context,R.color.list_item_buttons_text_color)
        })
    }
}


@BindingAdapter("downloadState")
fun downloadState(view: TextView, magazine: DomainMagazine?) {
    magazine?.let {
        view.text = magazine.getDownloadState().toString()
        view.isVisible = when (magazine.getDownloadState()) {
            DownloadState.PENDING, DownloadState.RUNNING, DownloadState.PAUSED -> true
            else -> false
        }
    }
}

@BindingAdapter("progressTrucking")
fun progressTrucking(view: CircularProgressBar, magazine: DomainMagazine?){
    magazine?.let {
        view.progress = when(magazine.getDownloadState()){
            DownloadState.EMPTY,DownloadState.PENDING -> 0F
            else -> magazine.downloadProgress.toFloat()
        }
            view.isVisible = when (magazine.getDownloadState()) {
            DownloadState.PENDING, DownloadState.RUNNING, DownloadState.PAUSED -> true
            else -> false
        }
        view.indeterminateMode = when(magazine.getDownloadState()){
            DownloadState.PENDING -> true
            else -> false
        }
    }
}

@BindingAdapter("threeDots")
fun threeDots(view: LottieAnimationView, magazine: DomainMagazine?){
    view.isVisible = magazine?.getDownloadState() == DownloadState.PENDING
}


@BindingAdapter("downloadProgress")
fun downloadProgress(view: TextView, magazine: DomainMagazine?) {
    magazine?.let {
        view.text =when(magazine.getDownloadState()){
            DownloadState.PENDING -> view.context.getString(R.string.waiting)
            else -> magazine.downloadProgress.toString() + " %"
        }
        view.isVisible = when(magazine.getDownloadState()){
            DownloadState.RUNNING,DownloadState.PENDING,DownloadState.PAUSED -> true
            else -> false
        }

    }
}

@BindingAdapter("downloadOrReadDetail")
fun downloadOrReadDetail(view: MaterialButton, magazine: DomainMagazine?){
    magazine?.let {
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
        view.backgroundTintList = when(magazine.getDownloadState()){
            DownloadState.RUNNING,DownloadState.PENDING -> ColorStateList.valueOf(ContextCompat.getColor(view.context,android.R.color.holo_red_dark))
            else -> ColorStateList.valueOf(ContextCompat.getColor(view.context,R.color.list_item_buttons_text_color))
        }
    }
}