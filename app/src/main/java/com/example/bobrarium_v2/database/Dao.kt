package com.example.bobrarium_v2.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DatabaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(entity: ChatEntity): Long
    @Delete
    suspend fun deleteChat(entity: ChatEntity)
    @Query("SELECT * FROM ChatEntity")
    fun getChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM MessageEntity WHERE chat_id LIKE :chatId")
    fun getChatMessages(chatId: Long): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessage(entity: MessageEntity): Long
}