package com.ssquare.myapplication.monokrome.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.ssquare.myapplication.monokrome.data.Header
import com.ssquare.myapplication.monokrome.data.User
import com.ssquare.myapplication.monokrome.util.AUTH_HEADER_KEY
import com.ssquare.myapplication.monokrome.util.HEADER_PATH
import okhttp3.MultipartBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import timber.log.Timber

const val BASE_URL = "http://192.168.1.45:3000/api/"
const val HEADER_URL = "${BASE_URL}images/$HEADER_PATH"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface MonokromeApiService {
    //header needs to be added

    @GET("issues")
    suspend fun getIssues(@retrofit2.http.Header(AUTH_HEADER_KEY) token: String?): List<NetworkMagazine>

    @POST("users")
    suspend fun register(@Body user: User): String

    @POST("auth")
    suspend fun login(@Body user: User): String


    @Multipart
    @POST("issues")
    suspend fun createIssue(
        @retrofit2.http.Header(AUTH_HEADER_KEY) token: String?,
        @Part("title") title: String,
        @Part("description") description: String,
        @Part image: MultipartBody.Part,
        @Part edition: MultipartBody.Part,
        @Part("releaseDate") releaseDate: Long
    ): NetworkMagazine


}

object MonokromeApi {
    val retrofitService: MonokromeApiService by lazy { retrofit.create(MonokromeApiService::class.java) }
}


suspend fun MonokromeApiService.loadFromServer(authToken: String?): MagazineListOrException {
    return try {
        val header = Header(imageUrl = HEADER_URL)
        val issues = this.getIssues(authToken)
        MagazineListOrException(issues, header, null)
    } catch (exception: Exception) {
        MagazineListOrException(null, null, exception);
    }
}

suspend fun MonokromeApiService.registerUser(user: User): AuthTokenOrException {
    return try {
        val authToken = this.register(user)
        Timber.d("authToken: $authToken")
        AuthTokenOrException(authToken, null)
    } catch (exception: Exception) {
        AuthTokenOrException(null, exception)
    }
}

suspend fun MonokromeApiService.loginUser(user: User): AuthTokenOrException {
    return try {
        val authToken = this.login(user)
        Timber.d("login authToken: $authToken")
        AuthTokenOrException(authToken, null)
    } catch (exception: Exception) {
        AuthTokenOrException(null, exception)
    }
}


suspend fun MonokromeApiService.uploadIssue(
    token: String?,
    title: String,
    description: String,
    image: MultipartBody.Part,
    edition: MultipartBody.Part,
    releaseDate: Long
): MagazineOrException {
    return try {
        val issue = this.createIssue(token, title, description, image, edition, releaseDate)
        return MagazineOrException(issue, null)
    } catch (exception: Exception) {
        MagazineOrException(null, exception)
    }
}
