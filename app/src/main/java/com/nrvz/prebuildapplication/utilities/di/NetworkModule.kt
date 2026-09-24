package com.nrvz.prebuildapplication.utilities.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nrvz.prebuildapplication.BuildConfig
import com.nrvz.prebuildapplication.network.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * ============================================================================
 *  NetworkModule — builds Retrofit once, for the whole app.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * THE CHAIN:  OkHttpClient  ->  Retrofit  ->  ApiService
 *
 *   OkHttpClient : the actual HTTP engine. Timeouts, interceptors, caching.
 *   Retrofit     : turns an interface's annotations into HTTP calls.
 *   ApiService   : your typed API. The only thing the Repository touches.
 *
 * Each @Provides method can take OTHER provided types as parameters, and Hilt
 * wires them together. `provideRetrofit(client: OkHttpClient)` is Hilt saying
 * "I already know how to make an OkHttpClient, here you go". That is dependency
 * injection: nobody calls a constructor.
 *
 * TIME-OUTS: 3 minutes is generous, matching RockyGo. The default 10 seconds is
 * too short for the slow mobile-data connections our users are actually on, and a
 * timeout shows up as a vague "no internet" to them.
 *
 * ⚠️ THE LOGGING INTERCEPTOR PRINTS REQUEST/RESPONSE BODIES.
 * BODY level on a release build leaks user data into Logcat and into any crash
 * report attached to it. That is why the level is driven by BuildConfig.DEBUG.
 * If you ever touch this file, keep that condition.
 */
@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        // Lenient parsing tolerates slightly malformed server JSON instead of
        // throwing JsonSyntaxException and blanking the screen.
        .setLenient()
        .create()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.MINUTES)
            .readTimeout(3, TimeUnit.MINUTES)
            .writeTimeout(3, TimeUnit.MINUTES)
            .addInterceptor(loggingInterceptor)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        gson: Gson,
    ): Retrofit =
        Retrofit.Builder()
            // Must end with "/" or Retrofit throws IllegalArgumentException at
            // startup: "baseUrl must end in /".
            .baseUrl(BuildConfig.BASE_API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}
