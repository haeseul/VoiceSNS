package com.example.voicesns

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

import com.example.voicesns.record.Record

data class RegisterRequest(
    val email: String,
    val password: String,
    val nickname: String
)

interface ApiService {
    @POST("user")
    fun registerUser(@Body request: RegisterRequest): Call<Void>


    // record
    @POST("/record/register")
    fun createRecord(@Body record: Record): Call<Record>

    @GET("/record/{recordId}")
    fun getRecord(@Path("recordId") recordId: Int): Call<Record>

    @GET("/record/user/{userId}")
    fun getUserRecords(@Path("userId") userId: Int): Call<List<Record>>
}