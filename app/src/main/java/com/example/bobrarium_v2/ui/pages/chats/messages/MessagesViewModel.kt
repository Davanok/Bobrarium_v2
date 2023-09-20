package com.example.bobrarium_v2.ui.pages.chats.messages

import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bobrarium_v2.Resource
import com.example.bobrarium_v2.Simple
import com.example.bobrarium_v2.firebase.chat.FirebaseChatRepository
import com.example.bobrarium_v2.firebase.chat.Message
import com.example.bobrarium_v2.firebase.user.User
import com.example.bobrarium_v2.ui.pages.chats.chats.new_chat_dialog.setElements
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val repository: FirebaseChatRepository
): ViewModel() {
    private val _messagesState = Channel<Simple>()
    val messagesState = _messagesState.receiveAsFlow()
    val newMessagesCount = mutableIntStateOf(0)
    var oldMessagesCount = 0

    var authors = mutableStateListOf<User>()
    val messages = mutableStateListOf<Message>()

    private var onItemsChange: (items: List<Message>) -> Unit = {}
    fun setOnItemsChange(onChange: (List<Message>) -> Unit){
        onItemsChange = onChange
    }

    fun setMessagesObserver(chatId: String){
        viewModelScope.launch { _messagesState.send(Simple.Loading) }
        Firebase.database.getReference("messages/$chatId").addValueEventListener(
            object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val result = snapshot.children.map { Message(it) }
                    newMessagesCount.intValue = result.size - oldMessagesCount

                    viewModelScope.launch {
                        _messagesState.send(Simple.Success)
                    }
                    messages.setElements(result)
                    messages.forEach{ msg ->
                        if(msg.authorId !in authors.map { it.uid }){
                            repository.getUser(msg.authorId){ user ->
                                authors.add(user)
                            }
                        }
                    }
                    onItemsChange(result)
                }
                override fun onCancelled(error: DatabaseError) {
                    viewModelScope.launch { _messagesState.send(Simple.Fail(error.toException())) }
                }
            }
        )
    }

    fun sendMessage(uid: String, chatId: String, text: String) = viewModelScope.launch {
        repository.sendImage(uid, chatId, text)
            .collect{ result ->
            when(result){
//                is Resource.Loading -> if(result.data != null) messages.add(result.data)
//                is Resource.Success -> if(result.data != null) messages.add(result.data)
                is Resource.Error -> {Log.w("MyLog", result.message.toString())}
                is Resource.Loading -> {}
                is Resource.Success -> {}
            }
        }
    }
}