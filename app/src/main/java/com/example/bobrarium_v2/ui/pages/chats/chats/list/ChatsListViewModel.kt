package com.example.bobrarium_v2.ui.pages.chats.chats.list

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bobrarium_v2.CustomState
import com.example.bobrarium_v2.Resource
import com.example.bobrarium_v2.firebase.chat.Chat
import com.example.bobrarium_v2.firebase.user.UserRepository
import com.example.bobrarium_v2.ui.pages.chats.chats.new_chat_dialog.setElements
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatsListViewModel @Inject constructor(
    private val repository: UserRepository
): ViewModel() {
    private val _chatsState = Channel<CustomState<List<Chat>>>()
    val chatsState = _chatsState.receiveAsFlow()

    val chats = mutableStateListOf<Chat>()

    fun loadUserChats(uid: String = Firebase.auth.uid!!) = viewModelScope.launch {
        repository.getChats(uid).collect{ result ->
            when(result){
                is Resource.Loading -> _chatsState.send(CustomState(isLoading = true))
                is Resource.Success -> {
                    _chatsState.send(CustomState(isSuccess = result.data))
                    chats.setElements(result.data?: emptyList())
                }
                is Resource.Error -> _chatsState.send(CustomState(isError = result.message))
            }
        }
    }

}