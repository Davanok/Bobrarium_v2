package com.example.bobrarium_v2.ui.pages.account.auth.sign_in

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bobrarium_v2.CustomState
import com.example.bobrarium_v2.firebase.auth.AuthRepository
import com.example.bobrarium_v2.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val repository: AuthRepository
): ViewModel() {
    val email = mutableStateOf("")
    val password = mutableStateOf("")

    private val _signInState = Channel<CustomState<String>>()
    val signInState = _signInState.receiveAsFlow()

    fun loginUser() = viewModelScope.launch {
        repository.loginUser(email.value, password.value).collect{result ->
            when(result){
                is Resource.Success -> {
                    _signInState.send(CustomState(isSuccess = "success"))
                }
                is Resource.Loading -> {
                    _signInState.send(CustomState(isLoading = true))
                }
                is Resource.Error ->{
                    _signInState.send(CustomState(isError = result.message.toString()))
                }
            }
        }
    }
}