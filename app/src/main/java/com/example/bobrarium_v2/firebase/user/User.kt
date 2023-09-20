package com.example.bobrarium_v2.firebase.user

import android.net.Uri
import com.google.firebase.database.DataSnapshot
import com.google.firebase.storage.FirebaseStorage

data class User (
    val uid: String,
    val username: String?,
    val about: String?,
    val favouriteImage: String?,
    val chats: List<String>
){
    var fiUri: Uri? = null

    constructor(user: DataSnapshot): this(
        user.key as String,
        user.child("username").value as String?,
        user.child("about").value as String?,
        user.child("favouriteImage").value as String?,
        user.child("chats").children.map { it.value as String }
    )

    fun setUri(storage: FirebaseStorage, onSuccess: (Uri) -> Unit){
        if (fiUri != null) return
        storage.getReference("users/$uid/icons/$favouriteImage").downloadUrl.addOnSuccessListener {
            fiUri = it
            onSuccess(it)
        }
    }

    val map: Map<String, Any?>
        get() = mapOf(
            ::username.name to username,
            ::about.name to about,
            ::favouriteImage.name to favouriteImage
        )
}