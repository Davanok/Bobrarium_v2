package com.example.bobrarium_v2.ui.pages.chats.chats.new_chat_dialog

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bobrarium_v2.CustomState
import com.example.bobrarium_v2.Resource
import com.example.bobrarium_v2.firebase.chat.Chat
import com.example.bobrarium_v2.firebase.chat.FirebaseChatRepository
import com.example.bobrarium_v2.firebase.user.UserRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewChatDialogViewModel @Inject constructor(
    private val repository: FirebaseChatRepository,
    private val userRepository: UserRepository
): ViewModel() {
    val isLoading = mutableStateOf(false)
    private val _chats = Channel<CustomState<List<Chat>>>()
    val chatsState = _chats.receiveAsFlow()

    val filteredChats = mutableStateListOf<Chat>()

    fun loadChatsList() = viewModelScope.launch{
        repository.getChatsList().collect { result ->
            when(result){
                is Resource.Loading -> _chats.send(CustomState(isLoading = true))
                is Resource.Success -> {
                    _chats.send(CustomState(isSuccess = result.data))
                    filteredChats.setElements(result.data?: emptyList())
                }
                is Resource.Error -> _chats.send(CustomState(isError = result.message))
            }
        }
    }

    fun addChatForUser(
        chat: Chat,
        onSuccess: (String?) -> Unit,
        onFailure: (String?) -> Unit
    ) = viewModelScope.launch {
        userRepository.addChat(Firebase.auth.uid!!, chat.id).collect { result ->
            when(result){
                is Resource.Loading -> isLoading.value = true
                is Resource.Success -> {
                    isLoading.value = false
                    onSuccess(result.data)
                }
                is Resource.Error -> {
                    isLoading.value = false
                    onFailure(result.message)
                }
            }
        }
        repository.addMember(Firebase.auth.uid!!, chat.id)
    }
}