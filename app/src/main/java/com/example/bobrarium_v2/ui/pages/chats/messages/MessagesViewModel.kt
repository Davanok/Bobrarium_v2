package com.example.bobrarium_v2.ui.pages.chats.messages

import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bobrarium_v2.Resource
import com.example.bobrarium_v2.Simple
import com.example.bobrarium_v2.VisualContent
import com.example.bobrarium_v2.firebase.chat.FirebaseChatRepository
import com.example.bobrarium_v2.firebase.chat.Message
import com.example.bobrarium_v2.firebase.user.User
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MessagesViewModel"

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val repository: FirebaseChatRepository
): ViewModel() {
    val messagesState = mutableStateOf<Simple?>(null)
    var isPrivate: String? = null

    val newMessagesCount = mutableIntStateOf(0)

    val authors = mutableStateListOf<User>()
    val messages = mutableStateListOf<Message>()

    fun setMessagesObserver(chatId: String){
        val storage = Firebase.storage
        viewModelScope.launch { messagesState.value = Simple.Success }
        val reference = Firebase.database.getReference("messages/$chatId")

        reference.addChildEventListener(
            object : ChildEventListener{
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = Message(snapshot)

                    viewModelScope.launch {
                        delay(1000)
                        message.setUri(storage, chatId)
                        messages.add(message)
                    }
                    if(message.authorId !in authors.map { it.uid })
                        repository.getUser(message.authorId){ user ->
                            authors.add(user)
                        }
                    newMessagesCount.intValue ++
                    viewModelScope.launch { messagesState.value = Simple.Success }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = Message(snapshot)
                    message.setUri(storage, chatId)
                    messages.replaceAll {
                        if(it.id == message.id) message
                        else it
                    }
                    viewModelScope.launch { messagesState.value = Simple.Success }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    messages.removeIf { it.id == snapshot.key }
                    viewModelScope.launch { messagesState.value = Simple.Success }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {
                    viewModelScope.launch { messagesState.value = Simple.Fail(error.toException()) }
                }

            }
        )
    }

    fun sendMessage(uid: String, chatId: String, text: String, image: VisualContent?, onSuccess: (Int) -> Unit) = viewModelScope.launch {
        repository.sendMessage(uid, chatId, text, image, isPrivate).collect { result ->
            when(result){
                is Resource.Error -> {Log.w(TAG, result.message.toString())}
                is Resource.Loading -> {}
                is Resource.Success -> {
                    onSuccess(messages.lastIndex)
                }
            }
        }
    }
}