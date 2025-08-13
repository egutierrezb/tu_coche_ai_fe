package com.example.localapiclient

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VideosService {

    @GET("videos")
    fun getVideos(@Query("channel_id") channel_id: String, @Query("keyword") keyword: String): Call<VideosResponse>

}

data class VideosResponse(
    val results: List<String>
)