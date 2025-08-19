package com.example.localapiclient

import okhttp3.OkHttpClient
import okhttp3.Request
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
            //ipconfig getifaddr en1 for getting the ip address
            //we dont use the endpoint in localhost but GPU enabled VM hosted in TensorDock
            .baseUrl("http://91.108.80.251:47959")  //user your backend ip
            .client(httpClient)// use the okhttpclient instance with logger and timeouts
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val videoApi: VideosService by lazy {
        Retrofit.Builder()
            // ipconfig getifaddr en1 for getting the ip address
            //http://192.168.68.104:8001 -> this is IP for the backend server (same wifi ip)
            .baseUrl("https://egutierrezb.pythonanywhere.com/apivideos/")  //user your backend ip
            .client(httpClient)// use the okhttpclient instance with logger and timeouts
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VideosService::class.java)
    }

    /*val request: Request = Request.Builder()
        .url("http://192.168.68.104:8001/videos?channel_id=UC-kBlBK4icUzAN-2amwIRQA&keyword=Tsuru")
        .get()
        .build()*/
}
