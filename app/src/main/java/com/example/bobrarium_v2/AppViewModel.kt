package com.example.bobrarium_v2

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bobrarium_v2.firebase.chat.Chat
import com.example.bobrarium_v2.firebase.user.User
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AppViewModel(
    val database: FirebaseDatabase = Firebase.database
): ViewModel() {
    val appBarTitle = mutableStateOf<String?>(null)
    val chat = mutableStateOf<Chat?>(null)
    val author = mutableStateOf<User?>(null)
    val currentUser = mutableStateOf<User?>(null)


    fun setChatName(chatId: String, uid: String){
        if (chat.value == null || chat.value?.id != chatId) {
            viewModelScope.launch {
                chat.value = Chat.get(database.getReference("chats/$chatId").get().await(), uid) {
                    User(database.getReference("users/$it").get().await())
                }
                appBarTitle.value = chat.value!!.name
            }
        }
        else appBarTitle.value = chat.value!!.name
    }
}