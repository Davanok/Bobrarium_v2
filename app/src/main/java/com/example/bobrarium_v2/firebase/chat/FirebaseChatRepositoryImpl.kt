package com.example.bobrarium_v2.firebase.chat

import android.net.Uri
import com.example.bobrarium_v2.R
import com.example.bobrarium_v2.Resource
import com.example.bobrarium_v2.Simple
import com.example.bobrarium_v2.VisualContent
import com.example.bobrarium_v2.firebase.user.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val TAG = "FirebaseChatRepositoryImpl"

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
            else {
                val newChatRef = reference.push()
                val uid = listOfNotNull(auth.uid)
                val chat = Chat(newChatRef.key!!, name, about, imageName, uid, uid)
                newChatRef.setValue(chat.map)
                if (uri != null) {
                    storage.getReference("chats/${newChatRef.key}/${imageName}").putFile(uri)
                }
                emit(Resource.Success(chat))
            }
        }.catch {
            emit(Resource.Error(it.message))
        }
    }

//    override fun createPrivateChat(user1: String, user2: String, chatId: String) {
//        val chat = PrivateChat(chatId, user1, user2)
//        val reference = database.getReference("chats/$chatId")
//        reference.setValue(chat.map)
//        database.getReference("users/$user1/chats/$chatId").setValue(1)
//        database.getReference("users/$user2/chats/$chatId").setValue(1)
//    }

    override fun getChatsList(): Flow<Resource<List<Chat>>> {
        val ref =  database.getReference("chats")
        return flow {
            emit(Resource.Loading())
            val snapshot = ref.get().await()
            val chats = snapshot.children.mapNotNull { Chat.getNotPrivate(it) }.sortedBy { it.name }
            emit(Resource.Success(chats))
            chats.forEach{
                it.setUri(storage)
            }
            emit(Resource.Success(chats))
        }.catch {
            emit(Resource.Error(it.message))
        }
    }

    override fun getChat(chatId: String): Flow<Resource<Chat>> {
        val ref =  database.getReference("chats/$chatId")
        return flow {
            emit(Resource.Loading())
            val snapshot = ref.get().await()

            val chat = Chat.getNotPrivate(snapshot)
            if (chat == null) emit(Resource.Error(null))
            else {
                emit(Resource.Success(chat))
                chat.setUri(storage)
                emit(Resource.Success(chat))
            }
        }.catch {
            emit(Resource.Error(it.message))
        }
    }

    override fun sendMessage(uid: String, chatId: String, text: String, image: VisualContent?, isPrivate: String?): Flow<Resource<Message>> {
        return flow {
            val reference = database.getReference("messages/$chatId")
            if (isPrivate != null) reference.get().addOnSuccessListener {
                if (!it.exists()) {
                    database.getReference("users/$uid/chats/$chatId").setValue(isPrivate)
                    database.getReference("users/$isPrivate/chats/$chatId").setValue(uid)
                }
            }
            val msgRef = reference.push()
            val message = Message(msgRef.key!!, chatId, uid, text, image?.filename, null, Simple.Loading)
            emit(Resource.Loading(message))
            if(image!= null) {
                storage.getReference("chats/$chatId/messages/${msgRef.key!!}_${image.filename}").putFile(image.uri).await()
            }
            msgRef.setValue(message.map)
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
    private suspend fun getUser(uid: String): User {
        val user = User(database.getReference("users/$uid").get().await())
        user.setUri(storage)
        return user
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