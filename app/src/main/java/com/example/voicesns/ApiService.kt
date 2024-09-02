package com.example.voicesns

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class RegisterRequest(
    val email: String,
    val password: String,
    val nickname: String
)

interface ApiService {
    @POST("user")
    fun registerUser(@Body request: RegisterRequest): Call<Map<String, String>>
}