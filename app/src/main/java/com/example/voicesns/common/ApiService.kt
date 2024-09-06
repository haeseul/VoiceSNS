package com.example.voicesns.common

import com.example.voicesns.register.Message
import com.example.voicesns.register.User
import com.example.voicesns.record.Record
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    // user
    @POST("user")
    fun postUser(@Body user: User): Call<Message>

    @GET("user")
    fun getUserSelfInfo(): Call<User>

    // auth
    @POST("auth/login")
    fun login(@Body user: User): Call<Message>
    @POST("auth/refresh")
    fun refreshToken(
        @Header("Refresh-Token") refreshToken: String
    ): Call<Message>

    // record
    @POST("record/register")
    fun createRecord(@Body record: Record): Call<Record>

    @GET("record/{recordId}")
    fun getRecord(@Path("recordId") recordId: Int): Call<Record>

    @GET("record/user/{userId}")
    fun getUserRecords(@Path("userId") userId: Int): Call<List<Record>>
}