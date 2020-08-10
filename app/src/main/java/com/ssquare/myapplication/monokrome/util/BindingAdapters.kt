package com.ssquare.myapplication.monokrome.util

import android.graphics.drawable.ColorDrawable
import android.text.format.DateFormat
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.data.DomainMagazine
import com.ssquare.myapplication.monokrome.data.getDownloadState
import java.util.*


@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: GlideUrl?) {
    url?.let {

        Glide.with(view.context)
            .load(url)
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
    }

}

@BindingAdapter("previewOrDelete")
fun previewOrDelete(view: TextView, magazine: DomainMagazine?) {
    magazine?.let {
        view.text =
            if (magazine.getDownloadState() == DownloadState.COMPLETED) view.context.getString(R.string.delete) else view.context.getString(
                R.string.preview
            )
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

@BindingAdapter("downloadProgress")
fun downloadProgress(view: TextView, magazine: DomainMagazine?) {
    magazine?.let {
        view.text = magazine.downloadProgress.toString()
        view.isVisible =
            magazine.getDownloadState() == DownloadState.RUNNING// file is downloading }

    }
}
