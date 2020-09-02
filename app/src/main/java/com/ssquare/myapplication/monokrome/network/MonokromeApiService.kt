package com.ssquare.myapplication.monokrome.network

import com.ssquare.myapplication.monokrome.data.Header
import com.ssquare.myapplication.monokrome.data.User
import com.ssquare.myapplication.monokrome.util.AUTH_HEADER_KEY
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import retrofit2.http.*
import timber.log.Timber

const val BASE_URL = "http://192.168.1.5:3000/api/"
const val HEADER_PATH = "header.jpg"
const val HEADER_URL = "${BASE_URL}images/$HEADER_PATH"

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
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part image: MultipartBody.Part,
        @Part edition: MultipartBody.Part,
        @Part("releaseDate") releaseDate: Long
    ): NetworkMagazine
}


suspend fun MonokromeApiService.loadFromServer(authToken: String?): MagazineListOrError {
    return try {
        val header = Header(imageUrl = HEADER_URL)
        val issues = this.getIssues(authToken)
        MagazineListOrError(issues, header, null)
    } catch (throwable: Throwable) {
        Timber.e(throwable, "Monokrome Api Http exception")
        MagazineListOrError(null, null, throwable.convertToError())
    }
}

suspend fun MonokromeApiService.registerUser(user: User): AuthTokenOrError {
    return try {
        val authToken = this.register(user)
        Timber.d("authToken: $authToken")
        AuthTokenOrError(authToken, null)
    } catch (throwable: Throwable) {
        Timber.e(throwable, "Monokrome Api Http exception")
        AuthTokenOrError(null, throwable.convertToError())
    }
}

suspend fun MonokromeApiService.loginUser(user: User): AuthTokenOrError {
    return try {
        val authToken = this.login(user)
        Timber.d("login authToken: $authToken")
        AuthTokenOrError(authToken, null)
    } catch (throwable: Throwable) {
        Timber.e("Monokrome Api Http exception: ${throwable.message}")
        AuthTokenOrError(null, throwable.convertToError())
    }
}

suspend fun MonokromeApiService.uploadIssue(
    token: String?,
    title: String,
    description: String,
    image: MultipartBody.Part,
    edition: MultipartBody.Part,
    releaseDate: Long
): MagazineOrError {
    return try {
        val issue = this.createIssue(
            token,
            createPartFromString(title),
            createPartFromString(description),
            image,
            edition,
            releaseDate
        )
        return MagazineOrError(issue, null)
    } catch (throwable: Throwable) {
        Timber.e(throwable, "Monokrome Api exception: $throwable")
        MagazineOrError(null, throwable.convertToError())
    }
}

private fun createPartFromString(text: String): RequestBody {
    return RequestBody.create(
        MultipartBody.FORM, text
    )
}

fun Throwable.convertToError(): Error {
    return when (this) {
        is HttpException -> {
            val errorMessage = this.response()?.errorBody()?.string()
            Error(errorMessage, this.code())
        }
        else -> Error(this.message)
    }
}
