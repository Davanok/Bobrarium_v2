package com.example.bobrarium_v2.ui.pages.account.auth.sign_up

import android.util.Log
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
class SignUpViewModel @Inject constructor(
    private val repository: AuthRepository
): ViewModel() {
    val email = mutableStateOf("")
    val password = mutableStateOf("")

    private val _signUpState = Channel<CustomState<String>>()
    val signUpState = _signUpState.receiveAsFlow()

    fun registerUser() = viewModelScope.launch {
        Log.d("MyLog", "register user ${email.value}, ${password.value}")
        repository.registerUser(email.value, password.value).collect{result ->
            when(result){
                is Resource.Success -> {
                    _signUpState.send(CustomState(isSuccess = result.message.toString()))
                }
                is Resource.Loading -> {
                    _signUpState.send(CustomState(isLoading = true))
                }
                is Resource.Error ->{
                    _signUpState.send(CustomState(isError = result.message.toString()))
                }
            }
        }
    }
}