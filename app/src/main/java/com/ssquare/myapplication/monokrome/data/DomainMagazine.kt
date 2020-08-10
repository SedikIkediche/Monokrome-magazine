package com.ssquare.myapplication.monokrome.data

import com.bumptech.glide.load.model.GlideUrl
import com.ssquare.myapplication.monokrome.util.DownloadState
import com.ssquare.myapplication.monokrome.util.NO_DOWNLOAD
import com.ssquare.myapplication.monokrome.util.NO_FILE

data class DomainMagazine(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val releaseDate: Long = 0,
    val imageUrl: GlideUrl? = null,
    val editionUrl: String = "",
    var fileUri: String = NO_FILE,
    var downloadProgress: Int = -1,
    val downloadId: Int = NO_DOWNLOAD,
    var downloadState: Int = DownloadState.EMPTY.ordinal
)

fun DomainMagazine.getDownloadState() = DownloadState.values()[this.downloadState]