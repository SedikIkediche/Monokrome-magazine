package com.ssquare.myapplication.monokrome.data

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.ssquare.myapplication.monokrome.network.AuthTokenOrException
import com.ssquare.myapplication.monokrome.network.MonokromeApiService
import com.ssquare.myapplication.monokrome.network.loginUser
import com.ssquare.myapplication.monokrome.network.registerUser
import com.ssquare.myapplication.monokrome.util.NO_AUTH_TOKEN
import com.ssquare.myapplication.monokrome.util.getAuthToken
import com.ssquare.myapplication.monokrome.util.storeAuthToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AuthRepository(
    private val context: Context,
    private val network: MonokromeApiService
) {

    val _userState = MutableLiveData<AuthTokenOrException>()

    //check authToken existence
    fun checkForUserCreation() {
        //authServer.isUserCreated
    }

    //check authToken existence
    fun checkForUserLogin() =
        getAuthToken(context) != NO_AUTH_TOKEN

    suspend fun loginUser(email: String, password: String) {
        val auth = network.loginUser(User(email, password))
        storeAuthToken(context, auth.authToken ?: NO_AUTH_TOKEN)
        _userState.value = auth
    }

    suspend fun registerUser(email: String, password: String) {
        val auth = network.registerUser(User(email, password))
        storeAuthToken(context, auth.authToken ?: NO_AUTH_TOKEN)
        _userState.value = auth
    }

}