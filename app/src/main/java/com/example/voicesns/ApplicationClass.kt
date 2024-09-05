package com.example.voicesns

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

    val gson = GsonBuilder()
        .registerTypeAdapter(Timestamp::class.java, TimestampTypeAdapter())
        .registerTypeAdapter(ByteArray::class.java, ByteArrayTypeAdapter())
        .create()

    fun getClient(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return retrofit!!
    }
}