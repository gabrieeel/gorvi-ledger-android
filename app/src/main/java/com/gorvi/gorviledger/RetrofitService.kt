package com.gorvi.gorviledger

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.Date

object RetrofitService {
    private const val BASE_URL = BuildConfig.BASE_URL

    private const val APIKEY = BuildConfig.APIKEY

    private val apiKeyInterceptor = ApiKeyInterceptor(APIKEY)

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(apiKeyInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: Api = retrofit.create(Api::class.java)
}

interface Api {
    @POST("/auth/v1/token?grant_type=password")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @GET("/rest/v1/accounts?select=id,name,balances(balance,currency:currencies(code),updatedAt:updated_at)")
    suspend fun getBalances(@Header("Authorization") access_token: String): Response<List<BalanceResponse>>
}

data class LoginRequest(val email: String = "abc", val password: String)
data class LoginResponse(val access_token: String)

data class BalanceResponse(
    val id: Long,
    val name: String,
    val balances: List<Balance>
)

data class Balance(
    val balance: Long,
    val currency: Currency,
    val updatedAt: Date
)

data class Currency(
    val code: String
)
