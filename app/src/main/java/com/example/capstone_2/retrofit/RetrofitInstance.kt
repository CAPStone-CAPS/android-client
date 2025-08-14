package com.example.capstone_2.retrofit

import android.content.Context
import com.example.capstone_2.network.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val BASE_URL = "http://211.188.51.35/api/" // <-- include /api/ and trailing slash

    @Volatile private var retrofit: Retrofit? = null

    fun get(context: Context): Retrofit {
        return retrofit ?: synchronized(this) {
            val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor())
                .addInterceptor(logging)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()

            val instance = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            retrofit = instance
            instance
        }
    }
}
