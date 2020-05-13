package com.ssquare.myapplication.monokrome.ui.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ssquare.myapplication.monokrome.data.AuthRepository

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel(){

    private val _isUssrCreated = authRepository.checkForUserCreation()

    val isUssrCreated : LiveData<Boolean>
         get() = _isUssrCreated

    fun registerUser(email : String, password : String){
        authRepository.registerUser(email,password)
    }
}