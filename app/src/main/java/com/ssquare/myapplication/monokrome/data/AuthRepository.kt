package com.ssquare.myapplication.monokrome.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssquare.myapplication.monokrome.network.AuthTokenOrException
import com.ssquare.myapplication.monokrome.network.MonokromeApiService
import com.ssquare.myapplication.monokrome.network.loginUser
import com.ssquare.myapplication.monokrome.network.registerUser
import com.ssquare.myapplication.monokrome.util.deleteAuthToken
import com.ssquare.myapplication.monokrome.util.storeAuthToken

class AuthRepository(
    private val context: Context,
    private val network: MonokromeApiService
) {

    private val _userState = MutableLiveData<AuthTokenOrException>()
    val userState: LiveData<AuthTokenOrException>
        get() = _userState


    suspend fun loginUser(email: String, password: String) {
        val auth = network.loginUser(User(email, password))
        _userState.value = auth
        if (auth.authToken != null)
            storeAuthToken(context, auth.authToken)


    }

    suspend fun registerUser(email: String, password: String) {
        val auth = network.registerUser(User(email, password))
        _userState.value = auth
        if (auth.authToken != null)
            storeAuthToken(context, auth.authToken)
    }

    fun logoutUser() {
        deleteAuthToken(context)
        _userState.value = null
    }

}