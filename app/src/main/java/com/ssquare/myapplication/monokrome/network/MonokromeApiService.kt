package com.ssquare.myapplication.monokrome.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.ssquare.myapplication.monokrome.data.Header
import com.ssquare.myapplication.monokrome.util.AUTH_HEADER_KEY
import com.ssquare.myapplication.monokrome.util.AUTH_TOKEN
import com.ssquare.myapplication.monokrome.util.HEADER_PATH
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers

private const val BASE_URL = "http://192.168.1.3:3000/api/"
private const val HEADER_URL = "${BASE_URL}images/$HEADER_PATH"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface MonokromeApiService {
    //header needs to be added
    @Headers("$AUTH_HEADER_KEY: $AUTH_TOKEN")
    @GET("issues")
    suspend fun getIssues(): List<NetworkMagazine>

}

object MonokromeApi {
    val retrofitService: MonokromeApiService by lazy { retrofit.create(MonokromeApiService::class.java) }
}


suspend fun MonokromeApiService.loadFromServer(): MagazineListOrException {
    return try {
        val header = Header(imageUrl = HEADER_URL)
        val issues = this.getIssues()
        MagazineListOrException(issues, header, null)
    }catch (exception : Exception){
        MagazineListOrException(null, null, exception);
    }
}
