package com.ssquare.myapplication.monokrome.ui.auth.login

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssquare.myapplication.monokrome.data.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel @ViewModelInject constructor(
    private val authRepository: AuthRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val userState = authRepository.userState

    fun logInUser(email: String, password: String) {
        viewModelScope.launch {
            authRepository.loginUser(email, password)
        }
    }
}