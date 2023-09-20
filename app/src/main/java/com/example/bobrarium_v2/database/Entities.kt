package com.example.bobrarium_v2.database

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity
data class ChatEntity(
    @PrimaryKey(true) val id: Long? = null,
    val name: String,
)

@Entity
data class MessageEntity(
    @PrimaryKey(true) val id: Long? = null,
    @ColumnInfo("chat_id") val chatId: Long,
    @ColumnInfo("author_id") val authorId: String,
    val text: String
)

data class ChatWithMessages(
    @Embedded val chat: ChatEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "chat_id"
    )
    val messages: List<MessageEntity>
)
