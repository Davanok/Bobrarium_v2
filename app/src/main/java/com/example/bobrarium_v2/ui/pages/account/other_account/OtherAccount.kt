package com.example.bobrarium_v2.ui.pages.account.other_account

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bobrarium_v2.AppViewModel
import com.example.bobrarium_v2.CustomState
import com.example.bobrarium_v2.OnScrolledDown
import com.example.bobrarium_v2.R
import com.example.bobrarium_v2.firebase.user.User
import com.example.bobrarium_v2.stringSum
import com.example.bobrarium_v2.ui.Screen
import com.example.bobrarium_v2.ui.pages.account.AccountViewModel
import com.example.bobrarium_v2.ui.pages.chats.chats.chatSettings.FailCard
import com.example.bobrarium_v2.ui.pages.chats.chats.new_chat_dialog.DialogChatItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private const val TAG = "OtherAccount"

@Composable
fun OtherAccount(
    navController: NavHostController,
    uid: String,
    appViewModel: AppViewModel,
    viewModel: AccountViewModel = hiltViewModel()
) {

    val user by appViewModel.author
    viewModel.loadImages(uid)

    if((appViewModel.currentUser.value == null || appViewModel.currentUser.value?.uid != Firebase.auth.uid) && Firebase.auth.currentUser != null)
        viewModel.simpleLoadUser(Firebase.auth.uid!!){
            appViewModel.currentUser.value = it
        }

    if(user == null || user?.uid != uid){
        viewModel.loadUser(uid){
            appViewModel.author.value = it
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            CircularProgressIndicator()
        }
    }

    if (viewModel.loadChatsState.value == null && user != null && appViewModel.currentUser.value != null){
        val common = user!!.chats.intersect(appViewModel.currentUser.value!!.chats.toSet()).toList()
        viewModel.loadChatsList(common)
    }

    else {
        val state by viewModel.loadUserState.collectAsState(initial = CustomState())
        if (user != null)
            Content(navController, user!!, viewModel, appViewModel)
        else
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                if(state.isLoading) CircularProgressIndicator()
                else FailCard(state.isError)
            }
    }
}

@Composable
private fun Content(
    navController: NavHostController,
    user: User,
    viewModel: AccountViewModel,
    appViewModel: AppViewModel
){
    appViewModel.appBarTitle.value = user.username
    val collapseImages = remember { mutableStateOf(true) }
    Scaffold (
        floatingActionButton = {
            val context = LocalContext.current
            AnimatedVisibility(
                visible = user.uid != Firebase.auth.uid && collapseImages.value
            ) {
                FloatingActionButton(
                    onClick = {
                        val uid = Firebase.auth.uid
                        if (uid == null) Toast.makeText(context, R.string.not_signed, Toast.LENGTH_SHORT).show()
                        else navController.navigate(Screen.PrivateChat(stringSum(uid, user.uid)).route)
                    }
                ) {
                    Icon(imageVector = ImageVector.vectorResource(id = R.drawable.baseline_chat_24), contentDescription = stringResource(id = R.string.goToPrivateChat))
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        ) {
            val lazyListState = rememberLazyListState()

            lazyListState.OnScrolledDown { collapseImages.value = false }
            if (remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }.value == 0) collapseImages.value = true

            CollapsableImagesList(
                collapseImages,
                viewModel,
                user.about
            )
            if (appViewModel.currentUser.value != null) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val chats = viewModel.chatsList
                    Text(
                        modifier = Modifier.padding(start = 5.dp, end = 5.dp, top = 5.dp),
                        text = stringResource(id = if(chats.isNotEmpty()) R.string.commonChats else R.string.isNoCommonChats)
                    )
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ){
                        items(chats, key = { it.id }){chat ->
                            DialogChatItem(chat) {
                                navController.navigate(Screen.Chat(chat.id).route)
                            }
                        }
                    }
                }
            }
        }
    }
}