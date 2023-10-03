package com.example.bobrarium_v2.ui.pages.chats.messages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.bobrarium_v2.AppViewModel
import com.example.bobrarium_v2.R
import com.example.bobrarium_v2.Simple
import com.example.bobrarium_v2.VisualContent
import com.example.bobrarium_v2.database.DatabaseViewModel
import com.example.bobrarium_v2.firebase.chat.Message
import com.example.bobrarium_v2.getNameWithExtension
import com.example.bobrarium_v2.groupConsecutiveBy
import com.example.bobrarium_v2.isScrollingDown
import com.example.bobrarium_v2.pickImagesOnly
import com.example.bobrarium_v2.pickVisualMedia
import com.example.bobrarium_v2.ui.Screen
import com.example.bobrarium_v2.ui.pages.chats.chats.chatSettings.FailCard
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@Composable
fun Chat(
    navController: NavHostController,
    chatId: String,
    appViewModel: AppViewModel,
    databaseViewModel: DatabaseViewModel = viewModel(factory = DatabaseViewModel.factory),
    firebaseViewModel: MessagesViewModel = hiltViewModel()
) {
    val uid = Firebase.auth.uid
//    ProvideWindowInsets{
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        if(uid == null) FailCard(stringResource(id = R.string.not_signed))
        else
            when(firebaseViewModel.messagesState.value){
                null -> firebaseViewModel.setMessagesObserver(chatId)
                is Simple.Loading -> CircularProgressIndicator()
                is Simple.Fail -> FailCard(firebaseViewModel.messagesState.value?.err?.message)
                is Simple.Success -> {
                    appViewModel.setChatName(chatId, uid)
                    ChatContent(navController, uid, chatId, firebaseViewModel, appViewModel)
                }
            }
    }
//    }
}

@Composable
fun ChatContent(
    navController: NavHostController,
    uid: String,
    chatId: String,
    viewModel: MessagesViewModel,
    appViewModel: AppViewModel
){
    var showDialogMessage by remember {mutableStateOf<Message?>(null)}
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val state = rememberLazyListState()
        val scope = rememberCoroutineScope()
        Scaffold(
            modifier = Modifier.weight(1f),
            floatingActionButton = {
                AnimatedVisibility(
                    visible = state.isScrollingDown() && state.canScrollForward
                ) {
                    val isSecondClick = remember { mutableStateOf(false) }
                    if (viewModel.newMessagesCount.intValue == 0)
                        FloatingActionButton(onClick = {
                            scope.launch {
                                if (isSecondClick.value) {
                                    state.scrollToItem(viewModel.messages.lastIndex)
                                    isSecondClick.value = true
                                } else
                                    state.animateScrollToItem(viewModel.messages.lastIndex)
                            }.invokeOnCompletion {
                                isSecondClick.value = false
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = stringResource(id = R.string.scrollDown)
                            )
                        }
                    else
                        ExtendedFloatingActionButton(onClick = {
                            scope.launch {
                                if (isSecondClick.value) {
                                    state.scrollToItem(viewModel.messages.lastIndex)
                                    isSecondClick.value = true
                                } else
                                    state.animateScrollToItem(viewModel.messages.lastIndex)
                            }.invokeOnCompletion {
                                isSecondClick.value = false
                            }
                        }) {
                            Text(text = viewModel.newMessagesCount.intValue.toString())
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = stringResource(id = R.string.scrollDown)
                            )
                        }
                }
            }
        ) { padding ->
            if(state.canScrollBackward) {
                if(viewModel.messages.size - remember { derivedStateOf { state.firstVisibleItemIndex } }.value < 25) {
                    val lastIndex = viewModel.messages.lastIndex
                    if (lastIndex >= 0)
                        LaunchedEffect(key1 = state.canScrollBackward) {
                            scope.launch { state.animateScrollToItem(lastIndex) }
                        }
                }
            }
            else viewModel.newMessagesCount.intValue = 0

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                state = state
            ) {
                for ((index, group) in viewModel.messages.groupConsecutiveBy { it.authorId }
                    .withIndex()) {
                    val author =
                        viewModel.authors.firstOrNull { it.uid == group.firstOrNull()?.authorId }
                    userMessages(index, uid, author, group, viewModel,
                        {
                            appViewModel.author.value = author
                            if (author != null)
                                navController.navigate(Screen.OtherAccount(author.uid).route)
                        }, {
                            showDialogMessage = it
                        }
                    )
                }
            }
        }
        BottomTools(viewModel, uid, chatId, state)
    }
    if(showDialogMessage != null) ImageDialog(showDialogMessage!!){showDialogMessage = null}
}

@Composable
private fun BottomTools(viewModel: MessagesViewModel, uid: String, chatId: String, state: LazyListState){
    val scope = rememberCoroutineScope()
    var newMessageText by remember { mutableStateOf("") }
    var image by remember { mutableStateOf<VisualContent?>(null) }
    val context = LocalContext.current
    val launcher =
        pickVisualMedia{ uri ->
            if(uri == null) return@pickVisualMedia

            val filename = uri.getNameWithExtension(context)?: return@pickVisualMedia
            image = VisualContent(filename, uri)
        }
    if(image != null){
        Row (
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .padding(vertical = 5.dp),
                model = image!!.uri,
                contentDescription = stringResource(id = R.string.image))

            Text(
                modifier = Modifier
                    .padding(start = 10.dp, top = 5.dp, bottom = 5.dp, end = 10.dp)
                    .weight(1f),
                text = image!!.filename
            )

            IconButton(onClick = { image = null }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(id = R.string.cancel)
                )
            }
        }
    }
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth(),
        value = newMessageText,
        onValueChange = { newMessageText = it },
        label = { Text(text = stringResource(id = R.string.newMessage)) },
        leadingIcon = {
            IconButton(onClick = {
                launcher.pickImagesOnly()
            }) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_attach_file_24),
                    contentDescription = stringResource(id = R.string.attachFile)
                )
            }
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    viewModel.sendMessage(uid, chatId, newMessageText.trim(), image){
                        if (it >= 0)
                            scope.launch { state.scrollToItem(it) }
                    }
                    newMessageText = ""
                    image = null
                },
                enabled = newMessageText.isNotBlank() || image != null
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = stringResource(id = R.string.sendMessage)
                )
            }
        }
    )
}
@Composable
fun ImageDialog(message: Message, onDismiss: () -> Unit){
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box {
            AsyncImage(
                modifier = Modifier.align(Alignment.Center),
                model = message.imageUri,
                contentDescription = stringResource(id = R.string.image),
                contentScale = ContentScale.Inside
            )
        }
    }
}