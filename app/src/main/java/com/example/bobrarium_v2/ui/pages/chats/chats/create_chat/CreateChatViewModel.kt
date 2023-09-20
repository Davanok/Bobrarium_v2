package com.example.bobrarium_v2.ui.pages.chats.chats.create_chat

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bobrarium_v2.CustomState
import com.example.bobrarium_v2.Resource
import com.example.bobrarium_v2.firebase.chat.Chat
import com.example.bobrarium_v2.firebase.chat.FirebaseChatRepository
import com.example.bobrarium_v2.firebase.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateChatViewModel @Inject constructor(
    private val repository: FirebaseChatRepository,
    private val userRepository: UserRepository
): ViewModel() {
    private val _createState = Channel<CustomState<Chat>>()
    val createState = _createState.receiveAsFlow()

    fun createChat(name: String, about: String, imageName: String?, uri: Uri?) = viewModelScope.launch {
        repository.createChat(name, about, imageName, uri).collect{ result ->
            when(result){
                is Resource.Loading -> _createState.send(CustomState(isLoading = true))
                is Resource.Success -> _createState.send(CustomState(isSuccess = result.data))
                is Resource.Error -> _createState.send(CustomState(isError = result.message))
            }
        }
    }
    fun addChatForUser(uid: String?, chatId: String) = viewModelScope.launch {
        userRepository.addChat(uid, chatId).collect{ result ->
            when(result){
                is Resource.Success -> {}
                is Resource.Error -> Log.w("CreateChatViewModel", result.message.toString())
                is Resource.Loading -> {}
            }
        }
    }
}