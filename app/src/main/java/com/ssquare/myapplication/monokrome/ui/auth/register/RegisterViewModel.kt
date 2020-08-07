package com.ssquare.myapplication.monokrome.ui.auth.register

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssquare.myapplication.monokrome.data.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel @ViewModelInject constructor(
    private val authRepository: AuthRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val userState = authRepository.userState

    fun registerUser(email: String, password: String) {
        viewModelScope.launch {
            authRepository.registerUser(email, password)
        }
    }

}