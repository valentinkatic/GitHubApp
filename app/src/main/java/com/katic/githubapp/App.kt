package com.katic.githubapp

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import com.katic.githubapp.di.AppComponent
import com.katic.githubapp.di.DaggerAppComponent
import timber.log.Timber

val Context.appComponent get() = (applicationContext as App).appComponent
val Fragment.appComponent get() = context!!.appComponent

class App : Application() {

    companion object {
        private const val BASE_URL = "https://api.github.com"
        private const val BASE_AUTH_URL = "https://github.com"
        private const val AUTH_PREFS_FILENAME = "auth_prefs"
    }

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory()
            .create(this, BASE_URL, BASE_AUTH_URL, AUTH_PREFS_FILENAME)
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}