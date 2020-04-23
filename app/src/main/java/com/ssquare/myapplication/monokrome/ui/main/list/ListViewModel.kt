package com.ssquare.myapplication.monokrome.ui.main.list

import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.data.Repository

class ListViewModel(private val repository: Repository) : ViewModel() {

    val networkResponse = repository.networkResponse()
    val magazines = repository.getMagazineList()


    fun downloadFile(magazine: Magazine, context: Context) {
        //ask for storage permission
        //check internet connectivity
        val fileUrl = Uri.parse(magazine.editionUrl)
        val request = DownloadManager.Request(fileUrl)
            .setTitle(magazine.title)
            .setDescription(magazine.description)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        //set destination later

        val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)
    }
}

