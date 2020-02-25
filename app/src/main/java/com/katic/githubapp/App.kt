package com.katic.githubapp

import android.app.Application
import android.content.Context

val Context.appComponent get() = (applicationContext as App).appComponent

class App: Application() {

    private lateinit var _appComponent: AppInjector

    val appComponent get() = _appComponent

    override fun onCreate() {
        super.onCreate()
        _appComponent = AppInjector()
    }
}