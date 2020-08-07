package com.ssquare.myapplication.monokrome.ui.auth.login

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.ssquare.myapplication.monokrome.data.AuthRepository
import com.ssquare.myapplication.monokrome.network.AuthTokenOrException
import kotlinx.coroutines.launch

class LoginViewModel @ViewModelInject constructor(
    private val authRepository: AuthRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _userState = MutableLiveData<AuthTokenOrException>()
    val userState: LiveData<AuthTokenOrException>
        get() = _userState


    fun logInUser(email: String, password: String) {
        viewModelScope.launch {
            val auth = authRepository.loginUser(email, password)
            _userState.value = auth
        }
    }

}