package com.example.bobrarium_v2

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.bobrarium_v2.firebase.chat.Chat
import com.example.bobrarium_v2.firebase.user.User
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AppViewModel(
    val database: FirebaseDatabase = Firebase.database
): ViewModel() {
    val appBarTitle = mutableStateOf<String?>(null)
    val chatId = mutableStateOf<String?>(null)
    val chat = mutableStateOf<Chat?>(null)
    val author = mutableStateOf<User?>(null)
    val currentUser = mutableStateOf<User?>(null)


    fun setChatName(chatId: String){
        this.chatId.value = chatId
        database.getReference("chats/$chatId/name").get().addOnSuccessListener {
            appBarTitle.value = it.value as String?
        }
    }
}