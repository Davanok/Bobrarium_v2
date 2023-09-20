package com.example.bobrarium_v2.ui.pages.chats.chats.chatSettings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bobrarium_v2.AppViewModel
import com.example.bobrarium_v2.CustomState
import com.example.bobrarium_v2.R
import com.example.bobrarium_v2.Simple
import com.example.bobrarium_v2.firebase.chat.Chat
import com.example.bobrarium_v2.getNameWithExtension
import com.example.bobrarium_v2.pickImagesOnly
import com.example.bobrarium_v2.ui.pages.account.items.UsernameTextField
import com.example.bobrarium_v2.ui.pages.chats.chats.create_chat.ChatImage

@Composable
fun ChatSettings(
    navController: NavHostController,
    chatId: String,
    appViewModel: AppViewModel,
    viewModel: ChatInfoViewModel = hiltViewModel()
) {
    var chat by appViewModel.chat
    if(chat == null) viewModel.loadChat(chatId){
        chat = it
    }

    val state by viewModel.loadingChatState.collectAsState(initial = CustomState())
    if(chat != null) Content(chat!!, viewModel)
    else if (state.isSuccess != null) Content(state.isSuccess!!, viewModel)
    else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) CircularProgressIndicator()
            else FailCard(state.isError)
        }
    }
}

@Composable
private fun Content(chat: Chat, viewModel: ChatInfoViewModel){
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val state by viewModel.nonStopState

        if (state is Simple.Loading) LinearProgressIndicator(Modifier.fillMaxWidth())

        val context = LocalContext.current
        val uri = remember { mutableStateOf(chat.uri) }

        EditChatImage(uri.value){
            uri.value = it
            viewModel.updateImage(chat.id, it, it?.getNameWithExtension(context))
        }
        UsernameTextField(R.string.chatName, chat.name){
            viewModel.updateChatName(chat.id, it)
        }

    }
}
@Composable
private fun EditChatImage(uri: Uri?, onUriChanged: (Uri?) -> Unit){
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()){
            onUriChanged(it?: return@rememberLauncherForActivityResult)
        }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ){
        ChatImage(uri)
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = { launcher.pickImagesOnly() }
        ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(id = R.string.pickImage))
        }
        IconButton(
            modifier = Modifier.align(Alignment.BottomEnd),
            onClick = { onUriChanged(null) }
        ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(id = R.string.deleteImage))
        }
    }
}