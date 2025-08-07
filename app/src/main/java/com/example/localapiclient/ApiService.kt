package com.example.localapiclient

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("ask")
    fun askQuestion(@Body question: QuestionInput): Call<QuestionResponse>
}

data class QuestionInput(val question: String)
data class QuestionResponse(
    val answer: String,
    val best_car: String,
    val image_url: String?
)