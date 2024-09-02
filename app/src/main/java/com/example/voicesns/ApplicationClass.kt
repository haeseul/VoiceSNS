package com.example.voicesns

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApplicationClass {
    private const val API_URL = "http://10.0.2.2:8080/"
    private var retrofit: Retrofit? = null

    fun getClient(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}