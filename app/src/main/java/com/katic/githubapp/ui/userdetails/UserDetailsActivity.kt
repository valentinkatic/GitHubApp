package com.katic.githubapp.ui.userdetails

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.request.RequestOptions
import com.katic.api.model.User
import com.katic.githubapp.R
import com.katic.githubapp.appComponent
import com.katic.githubapp.databinding.ActivityUserDetailsBinding
import com.katic.githubapp.ui.common.GlideApp
import com.katic.githubapp.ui.search.SearchActivity
import com.katic.githubapp.util.UiUtils
import com.katic.githubapp.util.viewModelProvider
import timber.log.Timber
import java.util.concurrent.Callable

class UserDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USER_ID = "EXTRA_USER_ID"
    }

    private lateinit var viewBinder: ActivityUserDetailsBinding
    private val viewModel by viewModelProvider {
        UserDetailsViewModel(
            appComponent.apiRepository,
            appComponent.serviceInterceptor,
            intent.getStringExtra(EXTRA_USER_ID)
        )
    }

    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinder = ActivityUserDetailsBinding.inflate(layoutInflater)
        setContentView(viewBinder.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.userResult
            .observe(this, Observer {
                Timber.d("repoResult: $it")
                when {
                    it.isLoading -> viewBinder.progressBar.show()
                    it.isError -> {
                        viewBinder.progressBar.hide()
                        UiUtils.handleUiError(this, it.getException(true))
                    }
                    else -> {
                        viewBinder.progressBar.hide()
                        user = it.data
                        setUserInfo()
                    }
                }
            })
    }

    @SuppressLint("SetTextI18n")
    private fun setUserInfo() {
        GlideApp.with(this)
            .load(user?.avatarUrl)
            .apply(RequestOptions.circleCropTransform())
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(viewBinder.image)

        viewBinder.userTitle.text = user?.login
        viewBinder.userId.text = "User id: ${user?.id}"
        viewBinder.repos.text = "Public repositories: ${user?.publicRepos}"
        viewBinder.followers.text = "Followers: ${user?.followers}"
        viewBinder.following.text = "Following: ${user?.following}"
        viewBinder.created.text = "Created: ${user?.created}"
        viewBinder.updated.text = "Updated: ${user?.updated}"

        viewBinder.url.text = UiUtils.getSpannableString(
            "Url: ",
            user?.url ?: "",
            Callable {
                UiUtils.openUrl(this, user!!.url)
            }
        )
        viewBinder.url.movementMethod = LinkMovementMethod.getInstance()
        viewBinder.url.highlightColor = Color.TRANSPARENT
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (intent.getStringExtra(EXTRA_USER_ID) == null && !appComponent.serviceInterceptor.token.isNullOrEmpty()) {
            menuInflater.inflate(R.menu.menu_user_details, menu)
            return true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (viewBinder.progressBar.isVisible) {
            return false
        }
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.logout -> {
                viewModel.logout()
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
                finishAffinity()
                true
            }
            else -> false
        }
    }
}