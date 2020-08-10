package com.ssquare.myapplication.monokrome.data

import com.bumptech.glide.load.model.GlideUrl
import com.ssquare.myapplication.monokrome.util.DownloadState

data class DomainHeader(
    val id: Int = 0,
    val imageUrl: GlideUrl? = null
) {

    fun Magazine.getDownloadState() = DownloadState.values()[this.downloadState]
}