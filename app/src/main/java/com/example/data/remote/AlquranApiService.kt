package com.example.data.remote

import com.example.data.model.QuranSurahDetailResponse
import com.example.data.model.QuranSurahListResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface AlquranApiService {
    @GET("v1/surah")
    suspend fun getSurahsList(): QuranSurahListResponse

    @GET("v1/surah/{number}/{edition}")
    suspend fun getSurahDetail(
        @Path("number") surahNumber: Int,
        @Path("edition") edition: String // "quran-uthmani" or "quran-warsh"
    ): QuranSurahDetailResponse

    companion object {
        private const val BASE_URL = "https://api.alquran.cloud/"

        fun create(): AlquranApiService {
            val client = OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(AlquranApiService::class.java)
        }
    }
}
