package com.example.bobrarium_v2.firebase.chat

import android.net.Uri
import android.util.Log
import com.example.bobrarium_v2.firebase.user.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

private const val TAG = "FirebaseChatClass"

data class Chat(
    val id: String,
    val name: String?,
    val about: String? = null,
    val image: String?,
    val admins: List<String> = emptyList(),
    val members: List<String> = emptyList(),
    val isPrivate: User? = null,
    var uri: Uri? = null
){
    private constructor(snapshot: DataSnapshot): this(
        snapshot.key as String,
        snapshot.child("name").value as String?,
        snapshot.child("about").value as String?,
        snapshot.child("image").value as String?,
        snapshot.child("admins").children.mapNotNull { it.value as String? },
        snapshot.child("members").children.mapNotNull { it.value as String? }
    )
    private constructor(id: String, user: User): this(
        id = id,
        name = user.username,
        image = user.favouriteImage,
        isPrivate = user
    )

    suspend fun setUri(storage: FirebaseStorage){
        if (uri != null) return
        uri = isPrivate?.setGetUri(storage)
        if(image == null || uri != null || isPrivate != null) return
        try{
            uri = storage.getReference("chats/$id/$image").downloadUrl.await()
        }
        catch (e: Exception){
            Log.w(TAG, e)
        }
    }

    val map: Map<String, Any?>
        get() = mapOf(
            ::name.name to name,
            ::about.name to about,
            ::image.name to image,
            ::admins.name to admins,
            ::members.name to members
        )

    companion object {
//        suspend fun get(snapshot: DataSnapshot, uid: String, onGetUser: suspend (String) -> User): Chat?{
//            if (!snapshot.exists()) return null
//            if (snapshot.child("isPrivate").exists()) {
//                val uid1 = snapshot.child("uid1").value.toString()
//                val uid2 = snapshot.child("uid2").value.toString()
//                val user = onGetUser(if (uid == uid1) uid2 else uid1)
//                return Chat(
//                    snapshot.key!!,
//                    user
//                )
//            }
//            return Chat(snapshot)
//        }
        fun getPrivate(
            anotherUser: User,
            chatId: String
        ): Chat{
            return Chat(chatId, anotherUser)
        }
        fun getNotPrivate(snapshot: DataSnapshot) = if(snapshot.exists()) Chat(snapshot) else null
    }
}