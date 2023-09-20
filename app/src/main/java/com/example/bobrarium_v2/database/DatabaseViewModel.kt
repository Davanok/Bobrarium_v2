package com.example.bobrarium_v2.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.bobrarium_v2.App
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
class DatabaseViewModel(private val dao: DatabaseDao): ViewModel() {
    val chats = dao.getChats()

    fun getMessages(chatId: Long) = dao.getChatMessages(chatId)

    fun insertChat(chat: ChatEntity) = viewModelScope.launch {
        dao.insertChat(chat)
    }
    fun deleteChat(chat: ChatEntity) = viewModelScope.launch {
        dao.deleteChat(chat)
    }
    fun insertMessage(message: MessageEntity) = viewModelScope.launch {
        dao.insertMessage(message)
    }

    companion object {
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val dao = (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).appDatabase.dao
                return DatabaseViewModel(dao) as T
            }
        }
    }
}