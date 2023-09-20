package com.example.bobrarium_v2.ui.pages.chats.chats.chatSettings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bobrarium_v2.AppViewModel
import com.example.bobrarium_v2.CustomState
import com.example.bobrarium_v2.R
import com.example.bobrarium_v2.firebase.chat.Chat
import com.example.bobrarium_v2.firebase.user.User
import com.example.bobrarium_v2.ui.Screen
import com.example.bobrarium_v2.ui.pages.chats.chats.create_chat.ChatImage
import com.example.bobrarium_v2.ui.pages.chats.chats.new_chat_dialog.ItemImage

@Composable
fun ChatInfo(
    navController: NavHostController,
    chatId: String,
    appViewModel: AppViewModel,
    viewModel: ChatInfoViewModel = hiltViewModel()
) {
    viewModel.loadChat(chatId)
    ContentMain(navController, viewModel, appViewModel)
}

@Composable
fun FailCard(message: String? = null){
    Card {
        Icon(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(15.dp),
            imageVector = Icons.Default.Info,
            contentDescription = stringResource(id = R.string.fail)
        )
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(10.dp, 5.dp),
            text = stringResource(id = R.string.fail),
            style = MaterialTheme.typography.titleLarge
        )
        if (message != null)
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(10.dp, 5.dp),
                text = message
            )
    }
}
@Composable
private fun ContentMain(navController: NavHostController, viewModel: ChatInfoViewModel, appViewModel: AppViewModel){
    val state by viewModel.loadingChatState.collectAsState(initial = CustomState())

    if (state.isSuccess != null) {
        appViewModel.chat.value = state.isSuccess!!
        viewModel.loadUsers(state.isSuccess!!.members)
        Content(navController, state.isSuccess!!, appViewModel, viewModel)
    }
    else
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            if (state.isLoading) CircularProgressIndicator()
            else FailCard(message = state.isError)
        }
}
@Composable
private fun Content(navController: NavHostController, chat: Chat, appViewModel: AppViewModel, viewModel: ChatInfoViewModel){
    appViewModel.chatId.value = chat.id
    appViewModel.chat.value = chat
    LazyColumn(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        item(key = "header") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ){
                ChatImage(chat.uri)
            }
            Text(
                text = chat.name?: stringResource(id = R.string.bobeur),
                style = MaterialTheme.typography.titleLarge
            )
        }
        items(viewModel.chatMembers, key = {it.uid}){ user ->
            MembersItem(user, chat.admins.contains(user.uid)){
                navController.navigate(Screen.OtherAccount(user.uid).route)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MembersItem(user: User, isAdmin: Boolean, onClick: () -> Unit){
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemImage(user.fiUri)
            Column(modifier = Modifier.fillMaxHeight()) {
                Text(
                    text = user.username?: stringResource(id = R.string.bobeur),
                    style = MaterialTheme.typography.titleSmall
                )
                if (isAdmin){
                    Text(text = stringResource(id = R.string.admin))
                }
            }
        }
    }
}