package com.example.bobrarium_v2.firebase.user

import android.net.Uri
import android.util.Log
import com.example.bobrarium_v2.Resource
import com.example.bobrarium_v2.Simple
import com.example.bobrarium_v2.firebase.chat.Chat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage
) : UserRepository{
    companion object {
        private const val TAG = "UserRepositoryImpl"
    }
    override fun loadUser(uid: String): Flow<Resource<User>> {
        return flow {
            emit(Resource.Loading())
            val result = database.getReference("users/$uid").get().await()
            emit(Resource.Success(User(result)))
        }.catch {
            Log.w(TAG, it)
            emit(Resource.Error(it.message))
        }
    }

    override fun addChat(uid: String?, chatID: String): Flow<Resource<String?>> {
        return flow {
            if (uid == null) emit(Resource.Error("not auth"))
            else {
                emit(Resource.Loading())
                val reference = database.getReference("users/$uid/chats")
                if(!reference.get().await().children.map { it.value as String? }.contains(chatID))
                    reference.push().setValue(chatID).await()

                emit(Resource.Success(reference.key))
            }
        }.catch {
            Log.w(TAG, it)
            emit(Resource.Error(it.message))
        }
    }

    override fun getChats(uid: String): Flow<Resource<List<Chat>>> {
        return flow {
            emit(Resource.Loading())
            val chatsSnapshot = database.getReference("users/$uid/chats").get().await()
            val chatsID = chatsSnapshot.children.map { it.value.toString() }.distinct()
//            val result = mutableListOf<Chat>()
            val result = chatsID.map { Chat(database.getReference("chats/$it").get().await()) }
//            for (chatId in chatsID){
//                if(result.map{ it.id }.contains(chatId)) continue
//                val chat = database.getReference("chats/$chatId").get().await()
//                result.add(Chat(chat))
//            }
            emit(Resource.Success(result))
            result.forEach { chat ->
                chat.setUri(storage.reference)
                emit(Resource.Success(result))
            }
        }.catch {
            Log.w(TAG, it)
            emit(Resource.Error(it.message))
        }
    }

    override fun getChats(chatIds: List<String>): Flow<Resource<List<Chat>>> {
        return flow {
            emit(Resource.Loading())
            val result = chatIds.map { Chat(database.getReference("chats/$it").get().await()) }
            emit(Resource.Success(result))
            result.forEach { chat ->
                chat.setUri(storage.reference)
                emit(Resource.Success(result))
            }
        }.catch {
            Log.w(TAG, it)
            emit(Resource.Error(it.message))
        }
    }

    override fun getImages(uid: String): Flow<Resource<List<Uri>>> {

        val imagesRef = storage.getReference("users/$uid/icons").listAll()

        return flow{
            emit(Resource.Loading())
            val list = imagesRef.await()
            val result = list.items.map { it.downloadUrl.await() }
            emit(Resource.Success(result))
        }.catch {
            Log.w(TAG, it)
            emit(Resource.Error(it.message.toString()))
        }
    }

    override fun addImage(uid: String, uri: Uri, filename: String): Flow<Simple> {
        val ref = storage.getReference("users/$uid/icons/$filename")
        return flow {
            emit(Simple.Loading)
            ref.putFile(uri)
            makeImageFavourite(uid, filename)
            emit(Simple.Success)
        }.catch {
            Log.w(TAG, it)
            emit(Simple.Fail(it))
        }
    }

    override fun makeImageFavourite(uid: String, filename: String) {
        database.getReference("users/$uid/favouriteImage").setValue(filename).addOnFailureListener {
            Log.w(TAG, it)
        }
    }

    override fun deleteImage(uid: String, filename: String): Flow<Simple> {
        return flow {
            emit(Simple.Loading)
            storage.getReference("users/$uid/icons/$filename").delete()
            emit(Simple.Success)
        }.catch {
            Log.w(TAG, it)
            emit(Simple.Fail(it))
        }
    }

    override fun updateUsername(uid: String, username: String) {
        database.getReference("users/$uid/username").setValue(username).addOnFailureListener {
            Log.w(TAG, it)
        }
    }

    override fun updateAbout(uid: String, about: String) {
        database.getReference("users/$uid/about").setValue(about).addOnFailureListener {
            Log.w(TAG, it)
        }
    }

}