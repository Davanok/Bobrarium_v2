package com.example.bobrarium_v2.ui.pages.chats.chats.new_chat_dialog

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bobrarium_v2.Resource
import com.example.bobrarium_v2.Simple
import com.example.bobrarium_v2.firebase.chat.Chat
import com.example.bobrarium_v2.firebase.chat.FirebaseChatRepository
import com.example.bobrarium_v2.firebase.user.User
import com.example.bobrarium_v2.firebase.user.UserRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewChatDialogViewModel @Inject constructor(
    private val repository: FirebaseChatRepository,
    private val userRepository: UserRepository
): ViewModel() {
    val chatsState = mutableStateOf<Simple?>(null)
    val usersState = mutableStateOf<Simple?>(null)
    val isLoading = mutableStateOf(chatsState.value is Simple.Loading)

    init {
        snapshotFlow { chatsState.value }.onEach {
            isLoading.value = it is Simple.Loading || usersState.value is Simple.Loading
        }.launchIn(viewModelScope)
        snapshotFlow { usersState.value }.onEach {
            isLoading.value = it is Simple.Loading || chatsState.value is Simple.Loading
        }.launchIn(viewModelScope)
    }

    val chats = mutableListOf<Chat>()
    val users = mutableListOf<User>()

    val filteredChats = mutableStateListOf<Chat>()
    val filteredUsers = mutableStateListOf<User>()

    fun loadChatsList() = viewModelScope.launch{
        repository.getChatsList().collect { result ->
            when(result){
                is Resource.Loading -> chatsState.value = Simple.Loading
                is Resource.Success -> {
                    chatsState.value = Simple.Success
                    chats.setElements(result.data?: emptyList())
                    filteredChats.setElements(chats)
                }
                is Resource.Error -> chatsState.value = Simple.Fail()
            }
        }
    }
    fun loadUsersList() = viewModelScope.launch{
        userRepository.getUsersList().collect { result ->
            when(result){
                is Resource.Loading -> chatsState.value = Simple.Loading
                is Resource.Success -> {
                    usersState.value = Simple.Success
                    users.setElements(result.data?: emptyList())
                    filteredUsers.setElements(users)
                }
                is Resource.Error -> chatsState.value = Simple.Fail()
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