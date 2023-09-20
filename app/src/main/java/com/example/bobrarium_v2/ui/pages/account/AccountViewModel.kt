package com.example.bobrarium_v2.ui.pages.account

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bobrarium_v2.CustomState
import com.example.bobrarium_v2.Resource
import com.example.bobrarium_v2.Simple
import com.example.bobrarium_v2.firebase.chat.Chat
import com.example.bobrarium_v2.firebase.user.User
import com.example.bobrarium_v2.firebase.user.UserRepository
import com.example.bobrarium_v2.ui.pages.chats.chats.new_chat_dialog.setElements
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val repository: UserRepository
): ViewModel() {
    val images = mutableStateListOf<Uri>()

    val isLoading = mutableStateOf(false)

    private val _loadUserState = Channel<CustomState<User>>()
    val loadUserState = _loadUserState.receiveAsFlow()

    fun loadImages(uid: String) = viewModelScope.launch {
        repository.getImages(uid).collect{
            when(it){
                is Resource.Success -> {
                    images.setElements(it.data?: emptyList())
                    isLoading.value = false
                }
                is Resource.Loading -> {
                    isLoading.value = true
                }
                is Resource.Error ->{
                    isLoading.value = false
                }
            }
        }
    }

    fun addImage(
        uri: Uri,
        filename: String,
        uid: String
    ) = viewModelScope.launch{
        repository.addImage(uid, uri, filename).collect{
            when(it){
                is Simple.Loading -> isLoading.value = true
                is Simple.Success -> {
                    isLoading.value = false
                    images.add(uri)
                }
                is Simple.Fail -> {
                    isLoading.value = false
                    Log.w(TAG, it.err)
                }
            }
        }
    }

    fun makeImageFavourite(uid: String, filename: String) = repository.makeImageFavourite(uid, filename)

    fun deleteImage(
        uid: String,
        filename: String,
        onSuccess: () -> Unit
    ) = viewModelScope.launch {
        repository.deleteImage(uid, filename).collect{
            when(it){
                is Simple.Loading -> isLoading.value = true
                is Simple.Success -> {
                    isLoading.value = false
                    onSuccess()
                }
                is Simple.Fail -> {
                    isLoading.value = false
                    Log.w(TAG, it.err)
                }
            }
        }
    }

    fun loadUser(uid: String, onSuccess: (User?) -> Unit = {}) = viewModelScope.launch{
        repository.loadUser(uid).collect{ result ->
            when(result){
                is Resource.Loading -> {
                    _loadUserState.send(CustomState(isLoading = true))
                    isLoading.value = true
                }
                is Resource.Success -> {
                    _loadUserState.send(CustomState(isSuccess = result.data))
                    isLoading.value = false
                    onSuccess(result.data)
                }
                is Resource.Error -> {
                    _loadUserState.send(CustomState(isError = result.message))
                    isLoading.value = false
                }
            }
        }
    }
    fun simpleLoadUser(uid: String, onSuccess: (User?) -> Unit = {}) = viewModelScope.launch {
        repository.loadUser(uid).collect { result ->
            when(result){
                is Resource.Loading -> isLoading.value = true
                is Resource.Success -> {
                    isLoading.value = false
                    onSuccess(result.data)
                }
                is Resource.Error -> isLoading.value = false
            }
        }
    }

    fun updateUsername(uid: String, username: String) = repository.updateUsername(uid, username)
    fun updateAbout(uid: String, about: String) = repository.updateAbout(uid, about)

    val chatsList = mutableStateListOf<Chat>()
    val loadChatsState = mutableStateOf<Simple?>(null)

    fun loadChatsList(chatIds: List<String>) = viewModelScope.launch {
        repository.getChats(chatIds).collect { result ->
            when(result){
                is Resource.Loading -> loadChatsState.value = Simple.Loading
                is Resource.Success -> {
                    chatsList.setElements(result.data?: emptyList())
                    loadChatsState.value = Simple.Success
                }
                is Resource.Error -> loadChatsState.value = Simple.Fail()
            }
        }
    }

    companion object {
        const val TAG = "AccountViewModel"
    }
}