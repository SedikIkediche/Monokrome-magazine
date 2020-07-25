package com.ssquare.myapplication.monokrome.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers

private const val BASE_URL = "http://192.168.1.4/api/"
private const val AUTH_TOKEN =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6NiwiaXNBZG1pbiI6dHJ1ZSwiaWF0IjoxNTk1NTAzMzI0fQ.mzWfcFy4i1HDl7D_J2AF48UC2P_2Mm52hQuBLcWmam0"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface MonokromeApiService {
    //header needs to be added
    @Headers("x-auth-token: $AUTH_TOKEN")
    @GET("issues")
    suspend fun getIssues(): List<NetworkMagazine>
}

object MonokromeApi {
    val retrofitService: MonokromeApiService by lazy { retrofit.create(MonokromeApiService::class.java) }
}
