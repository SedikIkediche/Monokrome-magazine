package com.ssquare.myapplication.monokrome.ui.pdf

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.shockwave.pdfium.PdfDocument.Bookmark
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.util.FileUtils
import com.ssquare.myapplication.monokrome.util.MAGAZINE_URI
import kotlinx.android.synthetic.main.activity_pdf_view.*
import timber.log.Timber

class PdfViewActivity : AppCompatActivity(),
    OnPageChangeListener,
    OnLoadCompleteListener,
    OnPageErrorListener {
    var pageNumber = 0
    var pdfFileName: String? = null
    val fileUri: Uri by lazy {
        intent.extras!!.getString(MAGAZINE_URI, FileUtils.NO_FILE).toUri()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_view)
        displayFromUri(applicationContext, fileUri)
    }


    private fun displayFromUri(context: Context, uri: Uri) {
        pdfFileName = FileUtils.getFileNameFromUri(context, uri)
        pdfView.fromUri(uri)
            .defaultPage(pageNumber)
            .onPageChange(this)
            .enableAnnotationRendering(true)
            .onLoad(this)
            .scrollHandle(DefaultScrollHandle(this))
            .swipeHorizontal(true)
            .spacing(10) // in dp
            .onPageError(this)
            .load()
    }


    fun printBookmarksTree(
        tree: List<Bookmark>,
        sep: String
    ) {
        for (b in tree) {
            Timber.e(String.format("%s %s, p %d", sep, b.title, b.pageIdx))
            if (b.hasChildren()) {
                printBookmarksTree(b.children, "$sep-")
            }
        }
    }

    override fun onPageChanged(page: Int, pageCount: Int) {
        pageNumber = page
        title = String.format("%s %s / %s", pdfFileName, page + 1, pageCount)
    }

    override fun loadComplete(nbPages: Int) {
        val meta = pdfView.documentMeta
        Timber.e("title = %s", meta.title)
        Timber.e("author = %s", meta.author)
        Timber.e("subject = %s", meta.subject)
        Timber.e("keywords = %s", meta.keywords)
        Timber.e("creator = %s", meta.creator)
        Timber.e("producer = %s", meta.producer)
        Timber.e("creationDate = %s", meta.creationDate)
        Timber.e("modDate = %s", meta.modDate)

        printBookmarksTree(pdfView.tableOfContents, "-")
    }

    override fun onPageError(page: Int, t: Throwable?) {
        Timber.d("pdf error: ${t?.message ?: "unknown error"} ")
    }
}
