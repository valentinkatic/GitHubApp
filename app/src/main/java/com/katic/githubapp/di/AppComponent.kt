package com.katic.githubapp.di

import android.content.Context
import com.katic.api.ApiRepository
import com.katic.api.AuthRepository
import com.katic.githubapp.ui.repositorydetails.RepositoryDetailsViewModel
import com.katic.githubapp.ui.search.SearchViewModel
import com.katic.githubapp.ui.userdetails.UserDetailsViewModel
import com.katic.githubapp.util.ServiceInterceptor
import dagger.BindsInstance
import dagger.Component
import dagger.Subcomponent
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance applicationContext: Context,
            @BindsInstance @Named("apiBaseUrl") apiBaseUrl: String,
            @BindsInstance @Named("authBaseUrl") authBaseUrl: String,
            @BindsInstance @Named("authPrefsFilename") authPrefsFilename: String
        ): AppComponent
    }

    val okHttpClient: OkHttpClient

    val apiRepository: ApiRepository

    val authRepository: AuthRepository

    val serviceInterceptor: ServiceInterceptor

    val searchViewModel: SearchViewModel

    val userDetailsViewModel: UserDetailsViewModel

    val repositoryDetailsSubComponentFactory: RepositoryDetailsSubComponent.Factory

    @Subcomponent
    interface RepositoryDetailsSubComponent {
        @Subcomponent.Factory
        interface Factory {
            fun create(
                @BindsInstance @Named("user") user: String?,
                @BindsInstance @Named("repo") repo: String?
            ): RepositoryDetailsSubComponent
        }

        val repositoryDetailsViewModel: RepositoryDetailsViewModel
    }
}