package com.example.bobrarium_v2.firebase.user

import android.net.Uri
import com.example.bobrarium_v2.Resource
import com.example.bobrarium_v2.Simple
import com.example.bobrarium_v2.firebase.chat.Chat
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun loadUser(uid: String): Flow<Resource<User>>
    fun addChat(uid: String?, chatID: String): Flow<Resource<String>>
    fun getChats(uid: String): Flow<Resource<List<Chat>>>
    fun getImages(uid: String): Flow<Resource<List<Uri>>>

    fun addImage(uid: String, uri: Uri, filename: String): Flow<Simple>
    fun makeImageFavourite(uid: String, filename: String)
    fun deleteImage(uid: String, filename: String): Flow<Simple>

    fun updateUsername(uid: String, username: String)
    fun updateAbout(uid: String, about: String)

    fun getChats(chatIds: List<String>): Flow<Resource<List<Chat>>>
}