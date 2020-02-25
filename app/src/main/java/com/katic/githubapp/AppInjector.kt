package com.katic.githubapp

import android.content.Context
import android.util.Log
import com.katic.api.ApiRepository
import com.katic.api.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

class AppInjector {

    companion object {
        const val BASE_URL = "https://api.github.com"
    }

    val okHttpClient: OkHttpClient by lazy {
        val clientBuilder = OkHttpClient.Builder()

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
//            clientBuilder.addInterceptor(OkHttpDelayInterceptor())

        // return
        clientBuilder.build()
    }

    val apiService: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }

    val apiRepository: ApiRepository by lazy {
        ApiRepository(apiService)
    }

}