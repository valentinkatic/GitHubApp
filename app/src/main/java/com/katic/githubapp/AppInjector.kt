package com.katic.githubapp

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.katic.api.ApiRepository
import com.katic.api.ApiService
import com.katic.api.AuthRepository
import com.katic.api.AuthService
import com.katic.githubapp.util.ServiceInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

class AppInjector(context: Context) {

    companion object {
        const val BASE_URL = "https://api.github.com"
        const val BASE_LOGIN_URL = "https://github.com"
        private const val PREFS_FILENAME = "auth_prefs"
    }

    private val authPrefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
    }

    val serviceInterceptor: ServiceInterceptor by lazy { ServiceInterceptor(authPrefs) }

    val okHttpClient: OkHttpClient by lazy {
        val clientBuilder = OkHttpClient.Builder()
        clientBuilder.addInterceptor(serviceInterceptor)

        // log all network calls
        val loggingInterceptor = HttpLoggingInterceptor(
            object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Log.v("http", message)
                }
            }
        )
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        clientBuilder.addNetworkInterceptor(loggingInterceptor)

        // return
        clientBuilder.build()
    }

    val apiService: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }

    val authService: AuthService by lazy {
        val retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_LOGIN_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(AuthService::class.java)
    }

    val apiRepository: ApiRepository by lazy {
        ApiRepository(apiService)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(authService)
    }

}