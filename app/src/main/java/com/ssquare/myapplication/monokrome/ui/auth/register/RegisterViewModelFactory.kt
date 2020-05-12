package com.ssquare.myapplication.monokrome.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssquare.myapplication.monokrome.data.AuthRepository

@Suppress("UNCHECKED_CAST")
class RegisterViewModelFactory(private val authRepository: AuthRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java))
            return RegisterViewModel(authRepository) as T
        else
            throw IllegalArgumentException("Unknown ViewModel Class")
    }
}