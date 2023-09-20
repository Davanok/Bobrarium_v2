package com.example.bobrarium_v2.ui.pages.chats.chats.chatSettings

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bobrarium_v2.CustomState
import com.example.bobrarium_v2.Resource
import com.example.bobrarium_v2.Simple
import com.example.bobrarium_v2.firebase.chat.Chat
import com.example.bobrarium_v2.firebase.chat.FirebaseChatRepository
import com.example.bobrarium_v2.firebase.user.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatInfoViewModel @Inject constructor(
    private val repository: FirebaseChatRepository
): ViewModel() {
    private val _loadingChatState = Channel<CustomState<Chat>>()
    val loadingChatState = _loadingChatState.receiveAsFlow()

    fun loadChat(chatId: String, onSuccess: (Chat?) -> Unit = {}) = viewModelScope.launch {
        repository.getChat(chatId).collect { result ->
            when(result){
                is Resource.Loading -> _loadingChatState.send(CustomState(isLoading = true))
                is Resource.Success -> {
                    _loadingChatState.send(CustomState(isSuccess = result.data))
                    onSuccess(result.data)
                }
                is Resource.Error -> _loadingChatState.send(CustomState(isError = result.message))
            }
        }
    }

    val chatMembers = mutableStateListOf<User>()
    fun loadUsers(users: List<String>) = viewModelScope.launch {
        users.forEach{ uid ->
            if(!chatMembers.map { it.uid }.contains(uid))
                repository.getUser(uid){ user ->
                    if(!chatMembers.map { it.uid }.contains(uid)) chatMembers.add(user)
                }
        }
    }

    val nonStopState = mutableStateOf<Simple?>(null)
    fun updateImage(chatId: String, uri: Uri?, name: String?) = viewModelScope.launch {
        repository.updateImage(chatId, uri, name).collect{
            nonStopState.value = it
        }
    }
    fun updateChatName(chatId: String, name: String) = viewModelScope.launch {
        repository.updateChatName(chatId, name)
    }
}