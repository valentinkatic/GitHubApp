package com.katic.githubapp.util

import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response

class ServiceInterceptor(var prefs: SharedPreferences) : Interceptor {

    companion object {
        private const val PREFS_ACC_TOKEN = "PREFS_ACC_TOKEN"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        if (request.header("No-Authentication") == null) {
            if (!token.isNullOrEmpty()) {
                val finalToken = "token $token"
                request = request.newBuilder()
                    .addHeader("Authorization", finalToken)
                    .build()
            }
        }

        return chain.proceed(request)
    }

    var token: String?
        get() = prefs.getString(PREFS_ACC_TOKEN, null)
        set(value) = prefs.edit().putString(PREFS_ACC_TOKEN, value).apply()

}