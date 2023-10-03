package com.example.bobrarium_v2.firebase.chat

import android.net.Uri
import com.example.bobrarium_v2.Simple
import com.google.firebase.database.DataSnapshot
import com.google.firebase.storage.FirebaseStorage

private const val TAG = "MessageClass"

data class Message(
    val id: String,
    val chatId: String,
    val authorId: String,
    val text: String,
    val image: String?,
    var imageUri: Uri?,
    val state: Simple,
){

    constructor(snapshot: DataSnapshot): this(
        snapshot.key as String,
        snapshot.child("chatId").value as String,
        snapshot.child("authorId").value as String,
        snapshot.child("text").value as String,
        snapshot.child("image").value as String?,
        null,
        Simple.Success
    )

    fun setUri(storage: FirebaseStorage, chatId: String, onSuccess: (Uri) -> Unit = {}){
        if (imageUri != null || image == null) return
        storage.getReference("chats/$chatId/messages/${id}_$image").downloadUrl.addOnSuccessListener {
            imageUri = it
            onSuccess(it)
        }
    }

    val map: Map<String, Any?>
        get() = mapOf(
            ::chatId.name to chatId,
            ::authorId.name to authorId,
            ::text.name to text,
            ::image.name to image
        )
}
