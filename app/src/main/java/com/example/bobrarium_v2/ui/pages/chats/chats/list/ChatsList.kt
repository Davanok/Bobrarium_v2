package com.example.bobrarium_v2.ui.pages.chats.chats.list

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bobrarium_v2.AppViewModel
import com.example.bobrarium_v2.CustomState
import com.example.bobrarium_v2.R
import com.example.bobrarium_v2.database.DatabaseViewModel
import com.example.bobrarium_v2.isScrollingUp
import com.example.bobrarium_v2.ui.Screen
import com.example.bobrarium_v2.ui.pages.chats.chats.new_chat_dialog.NewChatDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun ChatsList(
    navController: NavHostController,
    appViewModel: AppViewModel,
    databaseViewModel: DatabaseViewModel = viewModel(factory = DatabaseViewModel.factory),
    firebaseViewModel: ChatsListViewModel = hiltViewModel()
) {
    appViewModel.appBarTitle.value = stringResource(id = R.string.app_name)
    val userId = Firebase.auth.uid
    if(userId != null) {
        firebaseViewModel.loadUserChats(userId)
        Content(firebaseViewModel, navController, appViewModel)
    }
    else {
        val activity = LocalContext.current as? Activity
        AlertDialog(
            onDismissRequest = { activity?.finish() },
            confirmButton = { TextButton(onClick = { navController.navigate(Screen.SignUp.route) }) {
                Text(text = stringResource(id = R.string.signUp))
            } },
            title = { Text(text = stringResource(id = R.string.not_signed_title)) },
            text = { Text(text = stringResource(id = R.string.not_signed)) },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        )
    }
}
@Composable
private fun Content(
    viewModel: ChatsListViewModel,
    navController: NavHostController,
    appViewModel: AppViewModel
){
    val chatsState by viewModel.chatsState.collectAsState(initial = CustomState())
    val showDialog = remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            AnimatedVisibility(
                visible = listState.isScrollingUp(),
            ) {
                ExtendedFloatingActionButton(
                    onClick = { showDialog.value = true }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.newChat))
                    Text(text = stringResource(id = R.string.newChat))
                }
            }
        }
    ){ padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            if (chatsState.isLoading)
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            else
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    items(viewModel.chats, key = { it.id }) { chat ->
                        ChatPreview(chat) {
                            appViewModel.chat.value = chat
                            if (chat.isPrivate == null)
                                navController.navigate(Screen.Chat(chat.id).route)
                            else {
                                appViewModel.author.value = chat.isPrivate
                                navController.navigate(Screen.PrivateChat(chat.isPrivate.uid).route)
                            }
                        }
                    }
                }

            if (showDialog.value) {
                NewChatDialog(navController) {
                    showDialog.value = false
                }
            }
        }
    }
}
