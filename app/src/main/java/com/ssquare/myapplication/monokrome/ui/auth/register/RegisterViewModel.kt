package com.ssquare.myapplication.monokrome.ui.auth.register

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.ssquare.myapplication.monokrome.data.AuthRepository
import com.ssquare.myapplication.monokrome.network.AuthTokenOrException
import kotlinx.coroutines.launch

class RegisterViewModel @ViewModelInject constructor(
    private val authRepository: AuthRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _userState = MutableLiveData<AuthTokenOrException>()
    val userState: LiveData<AuthTokenOrException>
        get() = _userState

    fun registerUser(email: String, password: String) {
        viewModelScope.launch {
            val auth = authRepository.registerUser(email, password)
            _userState.value = auth
        }
    }


}