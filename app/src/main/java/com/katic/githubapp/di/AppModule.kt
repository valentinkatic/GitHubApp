package com.katic.githubapp.di

import android.content.Context
import android.content.SharedPreferences
import com.katic.api.ApiRepository
import com.katic.api.ApiService
import com.katic.api.AuthRepository
import com.katic.api.AuthService
import com.katic.githubapp.util.ServiceInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import javax.inject.Named
import javax.inject.Singleton

@Module
object AppModule {

    @Provides
    fun provideAuthPrefs(
        context: Context,
        @Named("authPrefsFilename") authPrefsFilename: String
    ): SharedPreferences {
        return context.getSharedPreferences(authPrefsFilename, Context.MODE_PRIVATE)
    }

    @Provides
    fun provideServiceInterceptor(authPrefs: SharedPreferences) =
        ServiceInterceptor(authPrefs)

    @Provides
    @Singleton
    fun provideOkHttpClient(serviceInterceptor: ServiceInterceptor): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()
        clientBuilder.addInterceptor(serviceInterceptor)

        // log all network calls
        val loggingInterceptor = HttpLoggingInterceptor(
            object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Timber.tag("http")
                    Timber.v(message)
                }
            }
        )
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        clientBuilder.addNetworkInterceptor(loggingInterceptor)

        // return
        return clientBuilder.build()
    }

    @Provides
    fun provideApiService(
        client: OkHttpClient,
        @Named("apiBaseUrl") apiBaseUrl: String
    ): ApiService {
        val retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl(apiBaseUrl)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    fun provideAuthService(
        client: OkHttpClient,
        @Named("authBaseUrl") authBaseUrl: String
    ): AuthService {
        val retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl(authBaseUrl)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        return retrofit.create(AuthService::class.java)
    }

    @Provides
    fun provideApiRepository(apiService: ApiService) =
        ApiRepository(apiService)

    @Provides
    fun provideAuthRepository(authService: AuthService) =
        AuthRepository(authService)

}