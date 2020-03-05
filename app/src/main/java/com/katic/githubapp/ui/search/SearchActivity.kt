package com.katic.githubapp.ui.search

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.katic.api.model.Repository
import com.katic.api.model.User
import com.katic.githubapp.R
import com.katic.githubapp.appComponent
import com.katic.githubapp.ui.login.LoginFragment
import com.katic.githubapp.ui.repositorydetails.RepositoryDetailsActivity
import com.katic.githubapp.ui.userdetails.UserDetailsActivity
import com.katic.githubapp.util.UiUtils
import com.katic.githubapp.util.viewModelProvider
import kotlinx.android.synthetic.main.activity_search.*
import timber.log.Timber

class SearchActivity : AppCompatActivity(), SearchAdapter.Listener, LoginFragment.Listener {

    private val viewModel by viewModelProvider {
        SearchViewModel(appComponent.apiRepository)
    }

    private lateinit var searchAdapter: SearchAdapter
    private var loginAction: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        setupSearchView()
        setupRecycler()
        updateSortText()

        sortByText.setOnClickListener {
            showSortDialog()
        }

        observeViewModel()
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.fetchRepositories(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        val searchEditText: EditText = searchView.findViewById(R.id.search_src_text)
        val searchCloseButton: ImageView =
            searchView.findViewById(R.id.search_close_btn) as ImageView
        searchCloseButton.setOnClickListener {
            searchEditText.setText("")
            searchAdapter.swapData(emptyList(), 0)
        }
    }

    private fun setupRecycler() {
        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(
            repositoriesRecycler.context,
            layoutManager.orientation
        )
        repositoriesRecycler.layoutManager = layoutManager
        repositoriesRecycler.addItemDecoration(dividerItemDecoration)
        searchAdapter = SearchAdapter(this)
        repositoriesRecycler.adapter = searchAdapter
    }

    @SuppressLint("SetTextI18n")
    private fun updateSortText() {
        sortByText.text = "Sort by: ${viewModel.selectedSortItem}"
    }

    private fun showSortDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Sort by")
        val arrayAdapter = ArrayAdapter<String>(
            this,
            android.R.layout.select_dialog_singlechoice,
            viewModel.sortItems
        )
        builder.setSingleChoiceItems(
            arrayAdapter,
            viewModel.sortItems.indexOf(viewModel.selectedSortItem)
        ) { dialog, which ->
            val item = arrayAdapter.getItem(which)
            viewModel.onSelectedSortChange(item)
            dialog.dismiss()
        }

        builder.setNegativeButton(
            "cancel"
        ) { dialog, _ -> dialog.dismiss() }

        builder.show()
    }

    private fun observeViewModel() {
        viewModel.searchResult
            .observe(this, Observer {
                Timber.d("searchResult: $it")
                when {
                    it.isLoading -> progressBar.show()
                    it.isError -> {
                        progressBar.hide()
                        UiUtils.handleUiError(this, it.getException(true))
                    }
                    else -> {
                        progressBar.hide()
                        searchAdapter.swapData(
                            it.data?.allItems ?: emptyList(),
                            it.data?.totalCount ?: 0
                        )
                        updateSortText()
                    }
                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        loginAction = menu?.findItem(R.id.login)
        if (!appComponent.serviceInterceptor.token.isNullOrEmpty()) {
            loginAction!!.title = "My profile"
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (progressBar.isVisible) {
            return false
        }
        return when (item.itemId) {
            R.id.login -> {
                if (appComponent.serviceInterceptor.token.isNullOrEmpty()) {
                    val fragment = LoginFragment()
                    fragment.show(supportFragmentManager, null)
                } else {
                    val intent = Intent(this, UserDetailsActivity::class.java)
                    startActivity(intent)
                }
                true
            }
            else -> false
        }
    }

    //
    // SearchAdapter.Listener
    //

    override fun onRepositorySelected(repository: Repository) {
        Timber.d("onRepositorySelected: $repository")
        val intent = Intent(this, RepositoryDetailsActivity::class.java)
        intent.putExtra(RepositoryDetailsActivity.EXTRA_USER_ID, repository.owner.login)
        intent.putExtra(RepositoryDetailsActivity.EXTRA_REPO_ID, repository.name)
        startActivity(intent)
    }

    override fun onUserSelected(user: User) {
        Timber.d("onUserSelected: $user")
        val intent = Intent(this, UserDetailsActivity::class.java)
        intent.putExtra(UserDetailsActivity.EXTRA_USER_ID, user.login)
        startActivity(intent)
    }

    override fun fetchRepositoriesNext() {
        viewModel.fetchRepositoriesNext()
    }

    //
    // LoginFragment.Listener
    //

    override fun onSuccess() {
        loginAction!!.title = "My profile"
    }

}