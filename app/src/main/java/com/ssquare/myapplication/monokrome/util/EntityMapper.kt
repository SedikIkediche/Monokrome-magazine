package com.ssquare.myapplication.monokrome.util

import android.content.Context
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.ssquare.myapplication.monokrome.data.DomainHeader
import com.ssquare.myapplication.monokrome.data.DomainMagazine
import com.ssquare.myapplication.monokrome.data.Header
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.network.NetworkMagazine
import java.io.File
import java.net.URI

//map entities

fun List<NetworkMagazine>.toMagazines(context: Context): List<Magazine> {
    return this.map {
        it.toMagazine(context)
    }
}

fun List<Magazine>.toDomainMagazines(context: Context): List<DomainMagazine> {
    return this.map {
        it.toDomainMagazine(context)
    }
}

fun NetworkMagazine.toMagazine(context: Context): Magazine {
    val uri = FileUtils.createUriString(context, id)
    return if (File(URI.create(uri)).exists()) {
        Magazine(
            this.id,
            this.title,
            this.description,
            this.releaseDate,
            this.imageUrl,
            this.editionUrl,
            uri,
            100,
            downloadState = DownloadState.COMPLETED.ordinal
        )
    } else {
        Magazine(
            this.id,
            this.title,
            this.description,
            this.releaseDate,
            this.imageUrl,
            this.editionUrl
        )
    }
}

fun Magazine.toDomainMagazine(context: Context): DomainMagazine {
    return run {
        val authToken = getAuthToken(context) ?: ""
        val header = LazyHeaders.Builder().addHeader(AUTH_HEADER_KEY, authToken).build()
        val glideUrl = GlideUrl(this.imageUrl, header)

        DomainMagazine(
            id = this.id,
            title = this.title,
            description = this.description,
            releaseDate = this.releaseDate,
            imageUrl = glideUrl,
            editionUrl = this.editionUrl,
            fileUri = this.fileUri,
            downloadProgress = this.downloadProgress,
            downloadId = this.downloadId,
            downloadState = this.downloadState
        )
    }
}

fun DomainMagazine.toMagazine(): Magazine {
    val imageUrl = this.imageUrl?.toStringUrl() ?: ""

    return Magazine(
        this.id,
        this.title,
        this.description,
        this.releaseDate,
        imageUrl,
        this.editionUrl,
        this.fileUri,
        this.downloadProgress,
        this.downloadState
    )
}

fun DomainHeader.toHeader(): Header {
    val imageUrl = this.imageUrl?.toStringUrl() ?: ""
    return Header(this.id, imageUrl)
}

fun Header?.toDomainHeader(context: Context): DomainHeader? {

    return if (this != null) {
        val authToken = getAuthToken(context) ?: ""
        val header = LazyHeaders.Builder().addHeader(AUTH_HEADER_KEY, authToken).build()
        val glideUrl = GlideUrl(this.imageUrl, header)
        DomainHeader(this.id, glideUrl)
    } else null
}
