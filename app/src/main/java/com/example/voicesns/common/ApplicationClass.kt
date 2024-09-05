package com.example.voicesns.common

import android.content.Context
import com.example.voicesns.auth.AuthInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApplicationClass {
    private const val API_URL = "http://10.0.2.2:8080/"
    private var retrofit: Retrofit? = null

    fun getClient(context: Context): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context)) // context 전달
            .build()

        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(API_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    fun getURL(): String{
        return API_URL
    }
}
