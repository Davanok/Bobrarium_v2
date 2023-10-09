package com.example.bobrarium_v2.ui.pages.chats.messages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bobrarium_v2.AppViewModel
import com.example.bobrarium_v2.R
import com.example.bobrarium_v2.Simple
import com.example.bobrarium_v2.stringSum
import com.example.bobrarium_v2.ui.pages.account.AccountViewModel
import com.example.bobrarium_v2.ui.pages.chats.chats.chatSettings.FailCard
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun PrivateChat(
    navController: NavHostController,
    userId: String,
    appViewModel: AppViewModel,
    viewModel: MessagesViewModel = hiltViewModel(),
    accViewModel: AccountViewModel = hiltViewModel()
) {
    val uid = Firebase.auth.uid
    val user by appViewModel.author

    if(user == null || user?.uid != userId){
        accViewModel.loadUser(userId){
            appViewModel.author.value = it
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            CircularProgressIndicator()
        }
    }
    else{
        if (user!! !in viewModel.authors) viewModel.authors.add(user!!)
        if (appViewModel.appBarTitle.value != user!!.username) appViewModel.appBarTitle.value = user!!.username
        viewModel.isPrivate = user!!.uid
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        if(uid == null) FailCard(stringResource(id = R.string.not_signed))
        else {
            val chatId = stringSum(userId, uid)
            when (viewModel.messagesState.value) {
                null -> viewModel.setMessagesObserver(chatId)
                is Simple.Loading -> CircularProgressIndicator()
                is Simple.Fail -> FailCard(viewModel.messagesState.value?.err?.message)
                is Simple.Success -> {
                    if (user != null)
                        appViewModel.setPrivateChatName(chatId, user!!)
                    ChatContent(navController, uid, chatId, viewModel, appViewModel)
                }
            }
        }
    }
}