package com.example.bobrarium_v2.firebase.user

import android.net.Uri
import android.util.Log
import com.example.bobrarium_v2.Resource
import com.example.bobrarium_v2.Simple
import com.example.bobrarium_v2.firebase.chat.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
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

    override fun addChat(uid: String?, chatID: String): Flow<Resource<String>> {
        return flow {
            if (uid == null) emit(Resource.Error("not auth"))
            else {
                emit(Resource.Loading())
                val reference = database.getReference("users/$uid/chats/$chatID")
                if(!reference.get().await().exists())
                    reference.setValue(true).await()
                emit(Resource.Success(chatID))
            }
        }.catch {
            Log.w(TAG, it)
            emit(Resource.Error(it.message))
        }
    }

    override fun getChats(uid: String): Flow<Resource<List<Chat>>> {
        return flow {
            emit(Resource.Loading())
            val reference = database.getReference("users/$uid/chats")
            val chatsID = reference.get().await().children.mapNotNull {
                if (it.key != null && it.value != null)
                    Pair(it.key!!, it.value!!)
                else null
            }.distinct()
            val result = chatsID.mapNotNull { id ->
                val chat =
                    if (id.second is String) {
                        Log.d(TAG, "private chat")
                        val user = User(database.getReference("users/${id.second}").get().await())
                        Chat.getPrivate(user, id.first)
                    }
                    else {
                        Log.d(TAG, "normal chat")
                        Chat.getNotPrivate(
                            database.getReference("chats/${id.first}").get().await()
                        )
                    }
//                val ref = database.getReference("chats/$id").get().await()
//                val chat = Chat.get(ref, uid) { getUser(it) }
//                if (chat == null) reference.child(id).removeValue().addOnSuccessListener { Log.d(TAG, id) }
                chat
            }
            emit(Resource.Success(result))
            result.forEach { chat ->
                chat.setUri(storage)
            }
            emit(Resource.Success(result))
        }.catch {
            Log.w(TAG, it)
            emit(Resource.Error(it.message))
        }
    }
    private suspend fun getUser(uid: String): User {
        val user = User(database.getReference("users/$uid").get().await())
        user.setUri(storage)
        return user
    }

    override fun getChats(chatIds: List<String>): Flow<Resource<List<Chat>>> {
        return flow {
            emit(Resource.Loading())
            val result = chatIds.mapNotNull { chatId ->
                val ref = database.getReference("chats/$chatId").get().await()
                Chat.getNotPrivate(ref)
            }
            emit(Resource.Success(result))
            result.forEach { chat ->
                chat.setUri(storage)
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

    override fun getUsersList(): Flow<Resource<List<User>>> {
        val ref =  database.getReference("users")
        return flow {
            emit(Resource.Loading())
            val snapshot = ref.get().await()
            val users = snapshot.children.mapNotNull { User(it) }.sortedBy { it.username }
            emit(Resource.Success(users))
            users.forEach{
                it.setUri(storage)
            }
            emit(Resource.Success(users))
        }.catch {
            emit(Resource.Error(it.message))
        }
    }

}