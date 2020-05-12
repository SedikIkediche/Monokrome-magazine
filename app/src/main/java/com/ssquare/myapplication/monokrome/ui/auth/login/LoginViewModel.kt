package com.ssquare.myapplication.monokrome.ui.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ssquare.myapplication.monokrome.data.AuthRepository

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel(){

   private val _isUserSignedIn = authRepository.checkForUserLogin()

    val isUserSignedIn : LiveData<Boolean>
          get() = _isUserSignedIn

    fun logInUser(email : String, password : String) = authRepository.loginUser(email, password)
}