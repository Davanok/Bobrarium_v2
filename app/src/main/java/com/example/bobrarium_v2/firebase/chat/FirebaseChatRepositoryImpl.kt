package com.example.bobrarium_v2.firebase.chat

import android.net.Uri
import com.example.bobrarium_v2.R
import com.example.bobrarium_v2.Resource
import com.example.bobrarium_v2.Simple
import com.example.bobrarium_v2.firebase.user.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseChatRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : FirebaseChatRepository {

    override fun createChat(
        name: String,
        about: String,
        imageName: String?,
        uri: Uri?
    ): Flow<Resource<Chat>> {
        val reference = database.getReference("chats")
        return flow {
            emit(Resource.Loading())

            val chatsNames = reference.get().await().children.map { it.child("name").value.toString() }
            if(chatsNames.contains(name)) emit(Resource.Error(R.string.nameTaken.toString()))

            val newChatRef = reference.push()
            val uid = listOfNotNull(auth.uid)
            val chat = Chat(newChatRef.key!!, name, about, imageName, uid, uid)
            newChatRef.setValue(chat.map)
            if(uri != null){
                storage.getReference("chats/${newChatRef.key}/${imageName}").putFile(uri)
            }
            emit(Resource.Success(chat))
        }.catch {
            emit(Resource.Error(it.message))
        }
    }

    override fun getChatsList(): Flow<Resource<List<Chat>>> {
        val ref =  database.getReference("chats")
        return flow {
            emit(Resource.Loading())
            val snapshot = ref.get().await()
            val chats = snapshot.children.map { Chat(it) }
            emit(Resource.Success(chats))
            chats.forEach{
                it.setUri(storage.reference)
                emit(Resource.Success(chats))
            }
        }.catch {
            emit(Resource.Error(it.message))
        }
    }

    override fun getChat(chatId: String): Flow<Resource<Chat>> {
        val ref =  database.getReference("chats/$chatId")
        return flow {
            emit(Resource.Loading())
            val snapshot = ref.get().await()
            val chat = Chat(snapshot)
            emit(Resource.Success(chat))
            chat.setUri(storage.reference)
            emit(Resource.Success(chat))
        }.catch {
            emit(Resource.Error(it.message))
        }
    }

    override fun sendImage(uid: String, chatId: String, text: String): Flow<Resource<Message>> {
        return flow {
            val reference = database.getReference("messages/$chatId").push()
            val message = Message(reference.key!!, chatId, uid, text, Simple.Loading)
            emit(Resource.Loading(message))
            reference.setValue(message.map)
            emit(Resource.Success(message))
        }.catch {
            emit(Resource.Error(it.message))
        }
    }

    override fun addMember(uid: String, chatId: String) {
        database.getReference("chats/$chatId/members").push().setValue(uid)
    }

    override fun getUser(uid: String, onSuccess: (User) -> Unit) {
        database.getReference("users/$uid").get().addOnSuccessListener { snapshot ->
            val user = User(snapshot)
            user.setUri(storage){
                onSuccess(user)
            }
        }
    }

    override fun updateImage(chatId: String, uri: Uri?, name: String?): Flow<Simple> {
        return flow {
            emit(Simple.Loading)
            val reference = database.getReference("chats/$chatId/image")
            val snapshot = reference.get().await()

            val oldImage = snapshot.value as String?
            if (oldImage != name){
                reference.setValue(name)
                val storageRef = storage.getReference("chats/${chatId}")
                if(oldImage != null){
                    storageRef.child(oldImage).delete()
                }
                if (uri != null && name != null){
                    storageRef.child(name).putFile(uri)
                    emit(Simple.Success)
                }
                else if(name == null)
                    emit(Simple.Fail(msg = R.string.filenameIsBlanc))
            }

        }.catch {
            emit(Simple.Fail(it))
        }
    }

    override fun updateChatName(chatId: String, name: String): Flow<Simple> {
        return flow {
            emit(Simple.Loading)
            database.getReference("chats/$chatId/name").setValue(name)
            emit(Simple.Success)
        }.catch {
            emit(Simple.Fail(it))
        }
    }


}