package com.example.localapiclient

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor // Correct import for HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit // Import for TimeUnit

object RetrofitClient {
    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Log request and response bodies
    }

    val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging) // Add the interceptor
        .connectTimeout(30, TimeUnit.SECONDS) // Example timeouts
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.68.104:8000")  //user your backend ip
            .client(httpClient)// use the okhttpclient instance with logger and timeouts
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}