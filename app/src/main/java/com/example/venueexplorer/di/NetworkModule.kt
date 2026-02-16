package com.example.venueexplorer.di

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Network module for providing HTTP clients with different configurations.
 * Centralizes network client creation and configuration.
 */
object NetworkModule {
    
    /**
     * Provides a Retrofit client for standard REST API calls.
     * Configured with shorter timeouts and JSON parsing support.
     * 
     * @return Configured Retrofit instance
     */
    fun provideRetrofitClient(): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
        
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * Provides an OkHttpClient optimized for speed test operations.
     * Configured with longer timeouts to accommodate large file transfers.
     * 
     * @return Configured OkHttpClient for speed tests
     */
    fun provideSpeedTestClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Provides a general-purpose OkHttpClient.
     * Configured with medium-length timeouts for general use cases.
     * 
     * @return Configured OkHttpClient
     */
    fun provideDefaultOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }
}