package com.example.bobrarium_v2.firebase.chat

data class PrivateChat(
    val chatId: String,
    val user1: String,
    val user2: String
){

    val map: Map<String, Any?>
        get() = mapOf(
            "isPrivate" to true,
            "uid1" to user1,
            "uid2" to user2
        )
}
