package com.ssquare.myapplication.monokrome.ui.auth.register

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssquare.myapplication.monokrome.data.AuthRepository
import com.ssquare.myapplication.monokrome.network.AuthTokenOrException
import kotlinx.coroutines.launch

class RegisterViewModel @ViewModelInject constructor(
    private val authRepository: AuthRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _userState = authRepository._userState

    val userState: LiveData<AuthTokenOrException>
        get() = _userState

    fun registerUser(email: String, password: String) {
        viewModelScope.launch {
            authRepository.registerUser(email, password)
        }
    }

}