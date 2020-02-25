package com.katic.githubapp.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.widget.ContentLoadingProgressBar
import com.katic.githubapp.R

class BlockingProgress @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0):
    FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var progressBar: ContentLoadingProgressBar

    override fun onFinishInflate() {
        super.onFinishInflate()

        // add progress bar
        LayoutInflater.from(context).inflate(R.layout.common_blocking_progress_content, this, true)
        progressBar = findViewById(R.id.progressBar_progress)

        // make frame always visible
        // but make it clickable when progress is shown to block clicks to underlying views
        visibility = View.VISIBLE
        hide()
    }

    val isVisible get() = isClickable

    fun show() {
        isClickable = true
        progressBar.show()
    }

    fun hide() {
        isClickable = false
        progressBar.hide()
    }
}
