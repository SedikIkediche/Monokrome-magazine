package com.ssquare.myapplication.monokrome.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssquare.myapplication.monokrome.data.AuthRepository

@Suppress("UNCHECKED_CAST")
class LoginViewModelFactory(private val authRepository: AuthRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java))
            return LoginViewModel(authRepository) as T
        else
            throw IllegalArgumentException("Unknown ViewModel Class")
    }
}