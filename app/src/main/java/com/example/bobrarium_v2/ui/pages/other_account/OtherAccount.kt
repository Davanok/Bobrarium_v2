package com.example.bobrarium_v2.ui.pages.other_account

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.bobrarium_v2.AppViewModel
import com.example.bobrarium_v2.CustomState
import com.example.bobrarium_v2.R
import com.example.bobrarium_v2.firebase.user.User
import com.example.bobrarium_v2.ui.Screen
import com.example.bobrarium_v2.ui.pages.account.AccountViewModel
import com.example.bobrarium_v2.ui.pages.chats.chats.chatSettings.FailCard
import com.example.bobrarium_v2.ui.pages.chats.chats.new_chat_dialog.DialogChatItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun OtherAccount(
    navController: NavHostController,
    uid: String,
    appViewModel: AppViewModel,
    viewModel: AccountViewModel = hiltViewModel()
) {

    val user by appViewModel.author
    viewModel.loadImages(uid)

    if(appViewModel.currentUser.value == null || appViewModel.currentUser.value?.uid != Firebase.auth.uid)
        viewModel.simpleLoadUser(uid){
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
    Column(modifier = Modifier.fillMaxSize()) {
        val lazyListState = rememberLazyListState()
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
        ) {
            var scrolledY = 0f
            var previousOffset = 0
            OtherAccountImages(
                viewModel.images,
                Modifier
                    .padding(10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .graphicsLayer {
                        scrolledY += lazyListState.firstVisibleItemScrollOffset - previousOffset
                        translationY = scrolledY * 0.5f
                        previousOffset = lazyListState.firstVisibleItemScrollOffset
                    }
            )
        }
        if(!user.about.isNullOrBlank())
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.about),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = user.about
                )
            }
        if (appViewModel.currentUser.value != null) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                val chats = viewModel.chatsList
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(id = if(chats.isNotEmpty()) R.string.commonChats else R.string.isNoCommonChats)
                )
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxWidth(),
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OtherAccountImages(images: List<Uri>, modifier: Modifier){
    if(images.isNotEmpty())
        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth(),
            state = rememberPagerState{images.size}
        ) { position ->
            AsyncImage(
                modifier = modifier,
                model = images[position],
                contentDescription = stringResource(id = R.string.userIcon),
                contentScale = ContentScale.FillWidth
            )
        }
    else
        Image(
            modifier = modifier,
            painter = painterResource(id = R.mipmap.beaver_icon),
            contentDescription = stringResource(id = R.string.userIcon),
            contentScale = ContentScale.FillWidth
        )
}
