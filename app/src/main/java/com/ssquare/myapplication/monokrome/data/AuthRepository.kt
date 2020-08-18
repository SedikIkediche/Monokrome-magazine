package com.ssquare.myapplication.monokrome.data

import android.content.Context
import com.ssquare.myapplication.monokrome.network.AuthTokenOrException
import com.ssquare.myapplication.monokrome.network.MonokromeApiService
import com.ssquare.myapplication.monokrome.network.loginUser
import com.ssquare.myapplication.monokrome.network.registerUser
import com.ssquare.myapplication.monokrome.util.deleteAuthToken
import com.ssquare.myapplication.monokrome.util.storeAuthToken
import timber.log.Timber

class AuthRepository(
    private val context: Context,
    private val network: MonokromeApiService
) {


    suspend fun loginUser(email: String, password: String): AuthTokenOrException {
        val auth = network.loginUser(User(email, password))
        if (auth.authToken != null)
            storeAuthToken(context, auth.authToken)

        return auth
    }

    suspend fun registerUser(email: String, password: String): AuthTokenOrException {
        val auth = network.registerUser(User(email, password))
        Timber.d("authToken: $auth")
        if (auth.authToken != null)
            storeAuthToken(context, auth.authToken)

        return auth
    }

    fun logoutUser() {
        deleteAuthToken(context)
    }


}