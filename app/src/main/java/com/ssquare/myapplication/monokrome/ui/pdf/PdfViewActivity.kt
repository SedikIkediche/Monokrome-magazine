package com.ssquare.myapplication.monokrome.ui.pdf

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.shockwave.pdfium.PdfDocument.Bookmark
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.util.MAGAZINE_URI
import com.ssquare.myapplication.monokrome.util.NO_FILE
import kotlinx.android.synthetic.main.activity_pdf_view.*

class PdfViewActivity : AppCompatActivity(),
    OnPageChangeListener,
    OnLoadCompleteListener,
    OnPageErrorListener {
    private val TAG = "PdfViewActivity"
    var pageNumber = 0
    var pdfFileName: String? = null
    val fileUri: Uri by lazy {
        intent.extras!!.getString(MAGAZINE_URI, NO_FILE).toUri()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_view)
        displayFromUri(fileUri)
    }


    private fun displayFromUri(uri: Uri) {
        pdfFileName = getFileName(uri)
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

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor =
                contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.lastPathSegment
        }
        return result
    }

    fun printBookmarksTree(
        tree: List<Bookmark>,
        sep: String
    ) {
        for (b in tree) {
            Log.e(
                TAG,
                String.format("%s %s, p %d", sep, b.title, b.pageIdx)
            )
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
        Log.e(
            TAG,
            "title = " + meta.title
        )
        Log.e(
            TAG,
            "author = " + meta.author
        )
        Log.e(
            TAG,
            "subject = " + meta.subject
        )
        Log.e(
            TAG,
            "keywords = " + meta.keywords
        )
        Log.e(
            TAG,
            "creator = " + meta.creator
        )
        Log.e(
            TAG,
            "producer = " + meta.producer
        )
        Log.e(
            TAG,
            "creationDate = " + meta.creationDate
        )
        Log.e(
            TAG,
            "modDate = " + meta.modDate
        )

        printBookmarksTree(pdfView.tableOfContents, "-")
    }

    override fun onPageError(page: Int, t: Throwable?) {
        Log.d(TAG, "pdf error: ${t?.message ?: "unknown error"} ")
    }
}
