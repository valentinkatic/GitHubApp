package com.katic.githubapp.ui.repositorydetails

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.katic.api.log.Log
import com.katic.api.model.Repository
import com.katic.githubapp.R
import com.katic.githubapp.appComponent
import com.katic.githubapp.ui.userdetails.UserDetailsActivity
import com.katic.githubapp.util.UiUtils
import com.katic.githubapp.util.viewModelProvider
import kotlinx.android.synthetic.main.activity_repository_details.*
import java.util.concurrent.Callable


class RepositoryDetailsActivity : AppCompatActivity() {

    companion object {
        private val log = Log.getLog("RepositoryDetailsActivity")
        const val EXTRA_USER_ID = "EXTRA_USER_ID"
        const val EXTRA_REPO_ID = "EXTRA_REPO_ID"
    }

    private val viewModel by viewModelProvider {
        RepositoryDetailsViewModel(
            appComponent.apiRepository,
            intent.getStringExtra(EXTRA_USER_ID),
            intent.getStringExtra(EXTRA_REPO_ID)
        )
    }

    private var repository: Repository? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repository_details)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.repoResult
            .observe(this, Observer {
                if (Log.LOG) log.d("repoResult: $it")
                when {
                    it.isLoading -> progressBar.show()
                    it.isError -> {
                        progressBar.hide()
                        UiUtils.handleUiError(this, it.getException(true))
                    }
                    else -> {
                        progressBar.hide()
                        repository = it.data
                        setRepositoryInfo()
                    }
                }
            })
    }

    @SuppressLint("SetTextI18n")
    private fun setRepositoryInfo() {
        repoTitle.text = repository?.name
        description.text = repository?.description
        fullName.text = "Full Name: ${repository?.fullName}"
        repoId.text = "Repo id: ${repository?.id}"
        nodeId.text = "Node id: ${repository?.nodeId}"
        watchers.text = "Watchers: ${repository?.watchersCount}"
        forks.text = "Forks: ${repository?.forksCount}"
        issues.text = "Issues: ${repository?.openIssuesCount}"
        created.text = "Created: ${repository?.created}"
        updated.text = "Updated: ${repository?.updated}"
        language.text = "Language: ${repository?.language}"

        owner.text = UiUtils.getSpannableString(
            "Owner: ",
            repository?.owner?.login ?: "",
            Callable {
                if (Log.LOG) log.d("onUserSelected")
                val intent = Intent(this, UserDetailsActivity::class.java)
                intent.putExtra(UserDetailsActivity.EXTRA_USER_ID, repository?.owner?.login)
                startActivity(intent)
            }
        )
        owner.movementMethod = LinkMovementMethod.getInstance()
        owner.highlightColor = Color.TRANSPARENT

        url.text = UiUtils.getSpannableString(
            "Url: ",
            repository?.url ?: "",
            Callable {
                UiUtils.openUrl(this, repository!!.url)
            }
        )
        url.movementMethod = LinkMovementMethod.getInstance()
        url.highlightColor = Color.TRANSPARENT
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}