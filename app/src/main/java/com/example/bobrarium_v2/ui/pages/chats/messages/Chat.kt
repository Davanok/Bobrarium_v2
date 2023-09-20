package com.example.bobrarium_v2.ui.pages.chats.messages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bobrarium_v2.AppViewModel
import com.example.bobrarium_v2.R
import com.example.bobrarium_v2.Simple
import com.example.bobrarium_v2.database.DatabaseViewModel
import com.example.bobrarium_v2.groupConsecutiveBy
import com.example.bobrarium_v2.isScrollingDown
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
    val messagesState by firebaseViewModel.messagesState.collectAsState(initial = null)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        if(uid == null) FailCard(stringResource(id = R.string.not_signed))
        else
            when(messagesState){
                null -> firebaseViewModel.setMessagesObserver(chatId)
                is Simple.Loading -> CircularProgressIndicator()
                is Simple.Fail -> FailCard(messagesState?.err?.message)
                is Simple.Success -> Content(navController, uid, chatId, firebaseViewModel, appViewModel)
            }
    }
}

@Composable
private fun Content(
    navController: NavHostController,
    uid: String,
    chatId: String,
    firebaseViewModel: MessagesViewModel,
    appViewModel: AppViewModel
){
    appViewModel.setChatName(chatId)
    Column {
        val newMessageText = remember { mutableStateOf("") }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val state = rememberLazyListState()
            val scope = rememberCoroutineScope()

//            val showDownBtn = remember { mutableStateOf(false) }
//            showDownBtn.value = !state.isScrollingUp() && state.canScrollForward

            if(!state.canScrollForward) {
                firebaseViewModel.oldMessagesCount = firebaseViewModel.messages.size
                firebaseViewModel.newMessagesCount.intValue = 0
            }

            firebaseViewModel.setOnItemsChange{
                if(firebaseViewModel.messages.size - state.firstVisibleItemIndex < 25)
                    scope.launch{ state.animateScrollToItem(it.lastIndex) }
//                else showDownBtn.value = true
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                state = state
            ){
                for( (index, group) in firebaseViewModel.messages.groupConsecutiveBy { it.authorId }.withIndex()){
                    val author = firebaseViewModel.authors.firstOrNull{ it.uid == group.firstOrNull()?.authorId }
                    userMessages(index, uid, author, group, firebaseViewModel) {
                        appViewModel.author.value = author
                        if(author != null)
                            navController.navigate(Screen.OtherAccount(author.uid).route)
                    }
                }
            }

            this@Column.AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                visible = /*showDownBtn.value*/ state.isScrollingDown() && state.canScrollForward
            ) {
                val isSecondClick = remember { mutableStateOf(false) }
                ExtendedFloatingActionButton(onClick = {
                    scope.launch {
                        if(isSecondClick.value) {
                            state.scrollToItem(firebaseViewModel.messages.lastIndex)
                            isSecondClick.value = true
                        }
                        else
                            state.animateScrollToItem(firebaseViewModel.messages.lastIndex)
                    }.invokeOnCompletion{
                        isSecondClick.value = false
                    }
                }) {
                    if(firebaseViewModel.newMessagesCount.intValue != 0)
                        Text(text = firebaseViewModel.newMessagesCount.intValue.toString())
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(id = R.string.scrollDown)
                    )
                }
            }
        }
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = newMessageText.value,
            onValueChange = { newMessageText.value = it },
            label = { Text(text = stringResource(id = R.string.newMessage)) },
            trailingIcon = {
                IconButton(onClick = {
                    firebaseViewModel.sendMessage(uid, chatId, newMessageText.value)
                    newMessageText.value = ""
                }) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = stringResource(id = R.string.sendMessage))
                }
            }
        )
    }
}