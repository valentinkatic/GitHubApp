package com.katic.githubapp.ui.login

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.katic.githubapp.R
import com.katic.githubapp.appComponent
import com.katic.githubapp.databinding.LoginFragmentBinding
import com.katic.githubapp.util.UiUtils
import com.katic.githubapp.util.viewModelProvider
import timber.log.Timber

class LoginFragment : DialogFragment() {

    interface Listener {
        fun onSuccess()
    }

    private var _viewBinder: LoginFragmentBinding? = null
    private val viewBinder get() = _viewBinder!!
    private val viewModel by viewModelProvider {
        LoginViewModel(
            appComponent.authRepository,
            appComponent.serviceInterceptor
        )
    }

    private lateinit var listener: Listener

    override fun onAttach(context: Context) {
        Timber.v("onAttach: $this")
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
        _viewBinder = LoginFragmentBinding.inflate(inflater, container, false)
        return viewBinder.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinder.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                Timber.d("onPageStarted: $url")
                val code = extractAuthorizationCode(url)
                if (!code.isNullOrEmpty()) {
                    Timber.d("code: $code")
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

        viewBinder.closeButton.setOnClickListener { dismiss() }
        viewBinder.webView.loadUrl("https://github.com/login/oauth/authorize?client_id=1a884e38444ceda41fb7")

        observeViewModel()
    }

    override fun onDestroyView() {
        _viewBinder = null
        super.onDestroyView()
    }

    fun extractAuthorizationCode(authorizationCodeResponseUrl: String?): String? {
        val uri = Uri.parse(authorizationCodeResponseUrl)
        return uri.getQueryParameter("code")
    }

    private fun observeViewModel() {
        viewModel.tokenResult
            .observe(this, Observer {
                Timber.d("tokenResult: $it")
                when {
                    it.isLoading -> viewBinder.progressBar.show()
                    it.isError -> {
                        viewBinder.progressBar.hide()
                        UiUtils.handleUiError(requireActivity(), it.getException(true))
                    }
                    else -> {
                        viewBinder.progressBar.hide()
                        listener.onSuccess()
                        dismiss()
                    }
                }
            })
    }
}