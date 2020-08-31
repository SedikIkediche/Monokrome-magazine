package com.ssquare.myapplication.monokrome.ui.pdf

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import com.github.barteksc.pdfviewer.listener.OnPageScrollListener
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.shockwave.pdfium.PdfDocument.Bookmark
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.util.FileUtils
import com.ssquare.myapplication.monokrome.util.PDF_FILE_NAME
import com.ssquare.myapplication.monokrome.util.*
import com.ssquare.myapplication.monokrome.util.networkcheck.ConnectivityProvider
import com.ssquare.myapplication.monokrome.util.networkcheck.ConnectivityProvider.Companion.hasInternet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_pdf_view.*
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class PdfViewActivity : AppCompatActivity(), OnPageChangeListener,
    ConnectivityProvider.ConnectivityStateListener {

    @Inject
    lateinit var provider: ConnectivityProvider

    @Inject
    lateinit var downloadUtils: DownloadUtils

    var pageNumber = 0
    val pdfFileName: String by lazy {
        intent.extras!!.getString(PDF_FILE_NAME, FileUtils.NO_FILE)
    }
    lateinit var fadeIn: Animation
    lateinit var fadeOut: Animation

    val fadeInListener = object : Animation.AnimationListener {
        override fun onAnimationEnd(arg0: Animation) {
            pdfPageNumberView.startAnimation(fadeOut)
        }

        override fun onAnimationRepeat(arg0: Animation) {}
        override fun onAnimationStart(arg0: Animation) {
            pdfPageNumberView.visibility = View.VISIBLE
            Timber.d("on animation start called")
        }
    }

    val fadeOutListener = object : Animation.AnimationListener {
        override fun onAnimationEnd(arg0: Animation) {
            pdfPageNumberView.visibility = View.GONE
        }

        override fun onAnimationRepeat(arg0: Animation) {}
        override fun onAnimationStart(arg0: Animation) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_view)
        displayFromUri(applicationContext, pdfFileName)
        setUpAnimations()
    }

    private fun setUpAnimations() {
        fadeIn = AlphaAnimation(0.0f, 1.0f)
        fadeIn.duration = 400
        fadeOut = AlphaAnimation(1.0f, 0.0f)
        fadeOut.duration = 400
    }

    override fun onStart() {
        super.onStart()
        fadeIn.setAnimationListener(fadeInListener)
        fadeOut.setAnimationListener(fadeOutListener)
    }

    override fun onStop() {
        super.onStop()
        fadeIn.setAnimationListener(null)
        fadeOut.setAnimationListener(null)
    }

    private fun displayFromUri(context: Context, pdfFileName: String) {

        pdfView.useBestQuality(true)
        pdfView.fromFile(File( context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.path + "/.Downloads_PDF/" + pdfFileName))
            .defaultPage(pageNumber)
            .onPageChange(this)
            .enableAnnotationRendering(true)
            .enableAntialiasing(true)
            .swipeHorizontal(true)
            .pageSnap(true)
            .autoSpacing(true)
            .pageFling(true)
            .pageFitPolicy(FitPolicy.BOTH)
            .fitEachPage(false)
            .nightMode(false)
            .load()
    }

    override fun onPageChanged(page: Int, pageCount: Int) {
        pdfPageNumberView.startAnimation(fadeIn)
        pdfPageNumberView.text = page.toString()
        Timber.d("on page changed called")
    }

    override fun onStateChange(state: ConnectivityProvider.NetworkState) {
        if (!state.hasInternet() && isDownloadActive(this)) {
            downloadUtils.killActiveDownloads()
        }
    }
}
