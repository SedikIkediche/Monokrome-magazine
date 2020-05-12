package com.ssquare.myapplication.monokrome.data

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.ssquare.myapplication.monokrome.network.FirebaseAuthServer

class AuthRepository private constructor(private val authServer: FirebaseAuthServer) {

    companion object {

        var INSTANCE: AuthRepository? = null
        fun getInstance(authServer: FirebaseAuthServer): AuthRepository {
            var instance = INSTANCE
            if (instance == null) {
                instance = AuthRepository(authServer)
                INSTANCE = instance
            }
            return instance
        }

    }

   fun checkForUserCreation() = authServer.isUserCreated

   fun checkForUserLogin() = authServer.isUserSignedIn

    fun loginUser(email :String, password : String){
        authServer.loginUser(email, password)
    }

    fun registerUser(email : String, password : String){
        authServer.registerUser(email, password)
    }

}