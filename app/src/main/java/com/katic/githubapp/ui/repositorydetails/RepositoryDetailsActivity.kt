package com.katic.githubapp.ui.repositorydetails

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.katic.api.model.Repository
import com.katic.githubapp.appComponent
import com.katic.githubapp.databinding.ActivityRepositoryDetailsBinding
import com.katic.githubapp.ui.userdetails.UserDetailsActivity
import com.katic.githubapp.util.UiUtils
import com.katic.githubapp.util.viewModelProvider
import timber.log.Timber
import java.util.concurrent.Callable


class RepositoryDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USER_ID = "EXTRA_USER_ID"
        const val EXTRA_REPO_ID = "EXTRA_REPO_ID"
    }

    private lateinit var viewBinder: ActivityRepositoryDetailsBinding
    private val viewModel by viewModelProvider {
        appComponent.repositoryDetailsSubComponentFactory.create(
            user = intent.getStringExtra(EXTRA_USER_ID),
            repo = intent.getStringExtra(EXTRA_REPO_ID)
        ).repositoryDetailsViewModel
    }

    private var repository: Repository? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinder = ActivityRepositoryDetailsBinding.inflate(layoutInflater)
        setContentView(viewBinder.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.repoResult
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
                        repository = it.data
                        setRepositoryInfo()
                    }
                }
            })
    }

    @SuppressLint("SetTextI18n")
    private fun setRepositoryInfo() {
        viewBinder.repoTitle.text = repository?.name
        viewBinder.description.text = repository?.description
        viewBinder.fullName.text = "Full Name: ${repository?.fullName}"
        viewBinder.repoId.text = "Repo id: ${repository?.id}"
        viewBinder.nodeId.text = "Node id: ${repository?.nodeId}"
        viewBinder.watchers.text = "Watchers: ${repository?.watchersCount}"
        viewBinder.forks.text = "Forks: ${repository?.forksCount}"
        viewBinder.issues.text = "Issues: ${repository?.openIssuesCount}"
        viewBinder.created.text = "Created: ${repository?.created}"
        viewBinder.updated.text = "Updated: ${repository?.updated}"
        viewBinder.language.text = "Language: ${repository?.language}"

        viewBinder.owner.text = UiUtils.getSpannableString(
            "Owner: ",
            repository?.owner?.login ?: "",
            Callable {
                Timber.d("onUserSelected")
                val intent = Intent(this, UserDetailsActivity::class.java)
                intent.putExtra(UserDetailsActivity.EXTRA_USER_ID, repository?.owner?.login)
                startActivity(intent)
            }
        )
        viewBinder.owner.movementMethod = LinkMovementMethod.getInstance()
        viewBinder.owner.highlightColor = Color.TRANSPARENT

        viewBinder.url.text = UiUtils.getSpannableString(
            "Url: ",
            repository?.url ?: "",
            Callable {
                UiUtils.openUrl(this, repository!!.url)
            }
        )
        viewBinder.url.movementMethod = LinkMovementMethod.getInstance()
        viewBinder.url.highlightColor = Color.TRANSPARENT
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}