package com.katic.githubapp.ui.login

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.katic.api.log.Log
import com.katic.githubapp.R
import com.katic.githubapp.appComponent
import com.katic.githubapp.util.UiUtils
import com.katic.githubapp.util.viewModelProvider
import kotlinx.android.synthetic.main.login_fragment.*

class LoginFragment : DialogFragment() {

    interface Listener {
        fun onSuccess()
    }

    companion object {
        private val log = Log.getLog("LoginFragment")
    }

    private val viewModel by viewModelProvider {
        LoginViewModel(
            appComponent.authRepository,
            appComponent.serviceInterceptor
        )
    }

    private lateinit var listener: Listener

    override fun onAttach(context: Context) {
        if (Log.LOG) log.v("onAttach: $this")
        super.onAttach(context)
        val target = targetFragment
        val parent = parentFragment
        listener = if (target != null && target is Listener) {
            target
        } else if (parent != null && parent is Listener) {
            parent
        } else if (context is Listener) {
            context
        } else {
            throw RuntimeException("parent must implement LoginFragment.Listener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.login_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                if (Log.LOG) log.d("onPageStarted: $url")
                if (url?.contains("code=") == true) {
                    val code = url.substring(url.indexOf("code=") + 5)
                    if (Log.LOG) log.d("code: $code")
                    viewModel.fetchToken(code)
                } else {
                    super.onPageStarted(view, url, favicon)
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url)
                return false
            }
        }

        closeButton.setOnClickListener { dismiss() }
        webView.loadUrl("https://github.com/login/oauth/authorize?client_id=1a884e38444ceda41fb7")

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.tokenResult
            .observe(this, Observer {
                if (Log.LOG) log.d("tokenResult: $it")
                when {
                    it.isLoading -> progressBar.show()
                    it.isError -> {
                        progressBar.hide()
                        UiUtils.handleUiError(requireActivity(), it.getException(true))
                    }
                    else -> {
                        progressBar.hide()
                        listener.onSuccess()
                        dismiss()
                    }
                }
            })
    }
}