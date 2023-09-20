package com.example.bobrarium_v2.firebase.chat

import android.net.Uri
import com.example.bobrarium_v2.Resource
import com.example.bobrarium_v2.Simple
import com.example.bobrarium_v2.firebase.user.User
import kotlinx.coroutines.flow.Flow

interface FirebaseChatRepository {
    fun createChat(name: String, about: String, imageName: String?, uri: Uri?): Flow<Resource<Chat>>
    fun getChatsList(): Flow<Resource<List<Chat>>>
    fun getChat(chatId: String): Flow<Resource<Chat>>
    fun sendImage(uid: String, chatId: String, text: String): Flow<Resource<Message>>
    fun addMember(uid: String, chatId: String)

    fun getUser(uid: String, onSuccess: (User) -> Unit)

    fun updateImage(chatId: String, uri: Uri?, name: String?): Flow<Simple>
    fun updateChatName(chatId: String, name: String): Flow<Simple>
}