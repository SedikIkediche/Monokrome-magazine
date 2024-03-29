package com.ssquare.myapplication.monokrome.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ssquare.myapplication.monokrome.util.DownloadState
import com.ssquare.myapplication.monokrome.util.FileUtils
import com.ssquare.myapplication.monokrome.util.NO_DOWNLOAD

@Entity(tableName = "magazines")
data class Magazine(
    @PrimaryKey(autoGenerate = false)
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val releaseDate: Long = 0,
    val imageUrl: String = "",
    val editionUrl: String = "",
    var fileUri: String = FileUtils.NO_FILE,
    var downloadProgress: Int = -1,
    val downloadId: Int = NO_DOWNLOAD,
    var downloadState: Int = DownloadState.EMPTY.ordinal
)

fun Magazine.getDownloadState() = DownloadState.values()[this.downloadState]

