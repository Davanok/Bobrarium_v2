package com.example.bobrarium_v2.firebase.chat

import android.net.Uri
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await

private const val TAG = "FirebaseChatClass"

data class Chat(
    val id: String,
    val name: String?,
    val about: String?,
    val image: String?,
    val admins: List<String>,
    val members: List<String>,
    var uri: Uri? = null
){
    constructor(snapshot: DataSnapshot): this(
        snapshot.key as String,
        snapshot.child("name").value as String?,
        snapshot.child("about").value as String?,
        snapshot.child("image").value as String?,
        snapshot.child("admins").children.mapNotNull { it.value as String? },
        snapshot.child("members").children.mapNotNull { it.value as String? }
    )

    suspend fun setUri(ref: StorageReference){
        if(image == null) return
        try{
            uri = ref.child("chats/$id/$image").downloadUrl.await()
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
}