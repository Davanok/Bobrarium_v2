package com.example.bobrarium_v2.firebase.chat

import com.example.bobrarium_v2.Simple
import com.google.firebase.database.DataSnapshot

data class Message(
    val id: String,
    val chatId: String,
    val authorId: String,
    val text: String,
    val isLoading: Simple,
){

    constructor(snapshot: DataSnapshot): this(
        snapshot.key as String,
        snapshot.child("chatId").value as String,
        snapshot.child("authorId").value as String,
        snapshot.child("text").value as String,
        Simple.Success
    )

    val map: Map<String, Any?>
        get() = mapOf(
            ::chatId.name to chatId,
            ::authorId.name to authorId,
            ::text.name to text
        )
}
