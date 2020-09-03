package com.ssquare.myapplication.monokrome.data

import android.content.Context
import com.ssquare.myapplication.monokrome.network.AuthTokenOrError
import com.ssquare.myapplication.monokrome.network.MonokromeApiService
import com.ssquare.myapplication.monokrome.network.loginUser
import com.ssquare.myapplication.monokrome.network.registerUser
import com.ssquare.myapplication.monokrome.util.deleteAuthToken
import com.ssquare.myapplication.monokrome.util.storeAuthToken
import okhttp3.OkHttpClient
import timber.log.Timber

class AuthRepository(
    private val context: Context,
    private val network: MonokromeApiService,
    private val client: OkHttpClient
) {


    suspend fun loginUser(email: String, password: String): AuthTokenOrError {
        val auth = network.loginUser(User(email, password))
        if (auth.authToken != null)
            storeAuthToken(context, auth.authToken)

        return auth
    }

    suspend fun registerUser(email: String, password: String): AuthTokenOrError {
        val auth = network.registerUser(User(email, password))
        Timber.d("authToken: $auth")
        if (auth.authToken != null)
            storeAuthToken(context, auth.authToken)

        return auth
    }

    fun logoutUser() {
        deleteAuthToken(context)
    }

    fun cancelNetworkOperations() {
        client.dispatcher.cancelAll()
    }


}