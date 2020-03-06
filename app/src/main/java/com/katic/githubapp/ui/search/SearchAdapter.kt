package com.katic.githubapp.ui.search

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.katic.api.model.Repository
import com.katic.api.model.User
import com.katic.githubapp.R
import com.katic.githubapp.databinding.LoadingItemBinding
import com.katic.githubapp.databinding.RepositoryItemBinding
import com.katic.githubapp.di.GlideApp

class SearchAdapter(val listener: Listener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_REPOSITORY: Int = 0
        const val TYPE_LOADING: Int = 1
    }

    private var repositories: List<Repository> = emptyList()
    private var totalRepositoriesCount: Int = 0
    private var getRepositoriesNextCalled = false

    init {
        setHasStableIds(true)
    }

    inner class ViewHolder(itemBinding: RepositoryItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        private val parentLayout: ConstraintLayout = itemBinding.parentLayout
        private val thumbnail: ImageView = itemBinding.thumbnail
        private val title: TextView = itemBinding.title
        private val author: TextView = itemBinding.author
        private val watchers: TextView = itemBinding.watchers
        private val forks: TextView = itemBinding.forks
        private val issues: TextView = itemBinding.issues

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {
            val repository = repositories[position]

            title.text = repository.name
            author.text = repository.owner.login
            watchers.text = "Watchers: ${repository.watchersCount}"
            forks.text = "Forks: ${repository.forksCount}"
            issues.text = "Issues: ${repository.openIssuesCount}"

            GlideApp.with(itemView.context)
                .load(repository.owner.avatarUrl)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(thumbnail)

            parentLayout.setOnClickListener { listener.onRepositorySelected(repository) }
            thumbnail.setOnClickListener { listener.onUserSelected(repository.owner) }
            author.setOnClickListener { listener.onUserSelected(repository.owner) }

            if (!getRepositoriesNextCalled && totalRepositoriesCount > repositories.size && itemCount - position == 5) {
                listener.fetchRepositoriesNext()
                getRepositoriesNextCalled = true
            }
        }
    }

    inner class LoadingViewHolder(itemBinding: LoadingItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_REPOSITORY) ViewHolder(
            RepositoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        ) else LoadingViewHolder(
            LoadingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return when (repositories.size) {
            totalRepositoriesCount -> totalRepositoriesCount
            else -> repositories.size + 1
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_REPOSITORY) {
            (holder as ViewHolder).bind(position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (repositories.size == totalRepositoriesCount || repositories.size in (position + 1) until totalRepositoriesCount) TYPE_REPOSITORY else TYPE_LOADING
    }

    override fun getItemId(position: Int): Long {
        return if (getItemViewType(position) == TYPE_REPOSITORY) repositories[position].id.toLong() else return -1
    }

    fun swapData(repositories: List<Repository>, totalRepositoriesCount: Int) {
        this.repositories = repositories
        this.totalRepositoriesCount = totalRepositoriesCount
        getRepositoriesNextCalled = false
        notifyDataSetChanged()
    }

    interface Listener {
        fun onRepositorySelected(repository: Repository)
        fun onUserSelected(user: User)
        fun fetchRepositoriesNext()
    }
}