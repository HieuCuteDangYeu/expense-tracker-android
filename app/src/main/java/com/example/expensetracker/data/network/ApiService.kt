package com.example.expensetracker.data.network

import com.example.expensetracker.BuildConfig
import com.example.expensetracker.data.network.dto.SyncPayloadDto
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/sync") suspend fun syncData(@Body payload: SyncPayloadDto): retrofit2.Response<Unit>
}

// Simple interceptor to add Mock Auth Token
class AuthInterceptor(private val token: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request =
                chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
        return chain.proceed(request)
    }
}

object RetrofitClient {
    private val BASE_URL = "${BuildConfig.SUPABASE_URL}/rest/v1/"

    private val okHttpClient =
            OkHttpClient.Builder()
                    .addInterceptor(AuthInterceptor(BuildConfig.SUPABASE_ANON_KEY))
                    .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
    }
}
