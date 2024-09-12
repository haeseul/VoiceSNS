package com.example.voicesns.common

import android.content.Context
import com.example.voicesns.auth.AuthInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.*
import java.lang.reflect.Type
import java.sql.Timestamp
import android.util.Base64

class TimestampTypeAdapter : JsonSerializer<Timestamp>, JsonDeserializer<Timestamp> {
    override fun serialize(src: Timestamp, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.time)
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Timestamp {
        return Timestamp(json.asJsonPrimitive.asLong)
    }
}

class ByteArrayTypeAdapter : JsonSerializer<ByteArray>, JsonDeserializer<ByteArray> {
    override fun serialize(src: ByteArray, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(Base64.encodeToString(src, Base64.DEFAULT))
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ByteArray {
        return Base64.decode(json.asString, Base64.DEFAULT)
    }
}

object ApplicationClass {
    private const val API_URL = "http://10.0.2.2:8080/"
    private var retrofit: Retrofit? = null

    fun getClient(context: Context): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context)) // context 전달
            .build()

        val gson = GsonBuilder()
            .registerTypeAdapter(Timestamp::class.java, TimestampTypeAdapter())
            .registerTypeAdapter(ByteArray::class.java, ByteArrayTypeAdapter())
            .create()

        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(API_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return retrofit!!
    }

    fun getURL(): String{
        return API_URL
    }
}