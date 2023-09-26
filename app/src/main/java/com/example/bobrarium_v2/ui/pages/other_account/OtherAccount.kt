package com.example.bobrarium_v2.ui.pages.other_account

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.bobrarium_v2.AppViewModel
import com.example.bobrarium_v2.CustomState
import com.example.bobrarium_v2.OnScrolledDown
import com.example.bobrarium_v2.R
import com.example.bobrarium_v2.firebase.user.User
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
    val visibility = remember { mutableStateOf(true) }
    Column(modifier = Modifier
        .fillMaxSize()
//        .onSwipedDown { visibility.value = true }
    ) {
        val lazyListState = rememberLazyListState()

//        if (lazyListState.canScrollForward)
        lazyListState.OnScrolledDown { visibility.value = false }
        if (remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }.value == 0) visibility.value = true

        CollapsableImage(visibility, viewModel)
        if(!user.about.isNullOrBlank() && visibility.value)
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
@Composable
private fun CollapsableImage(visible: MutableState<Boolean>, viewModel: AccountViewModel){
    AnimatedContent(
        targetState = visible.value,
        label = stringResource(id = R.string.userIcon),
        transitionSpec = {
            fadeIn(animationSpec = tween(150, 150)) togetherWith
                    fadeOut(animationSpec = tween(150)) using
                    SizeTransform { initialSize, targetSize ->
                        if (targetState) {
                            keyframes {
                                IntSize(targetSize.width, initialSize.height) at 150
                                durationMillis = 300
                            }
                        } else {
                            keyframes {
                                IntSize(initialSize.width, targetSize.height) at 150
                                durationMillis = 300
                            }
                        }
                    }
        }
    ) { targetState ->
        if (targetState) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                OtherAccountImages(
                    viewModel.images,
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clickable { visible.value = false },
                )
            }
        }
        else
            SmallOtherAccountImages(
                viewModel.images,
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                visible.value = true
            }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OtherAccountImages(images: List<Uri>, modifier: Modifier){
    if(images.isNotEmpty())
        Column(modifier = Modifier.fillMaxWidth()) {
            val pagerState = rememberPagerState{images.size}
            HorizontalPager(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, top = 10.dp),
                state = pagerState
            ) { position ->
                AsyncImage(
                    modifier = modifier
                        .clip(RoundedCornerShape(16.dp)),
                    model = images[position],
                    contentDescription = stringResource(id = R.string.userIcon),
                    contentScale = ContentScale.FillWidth
                )
            }
            Row(
                Modifier
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount){
                    val color =
                        if (pagerState.currentPage == it) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.background
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .align(Alignment.Bottom)
                            .background(color)
                            .size(10.dp)
                    )
                }
            }
        }
    else
        Image(
            modifier = modifier
                .clip(RoundedCornerShape(16.dp)),
            painter = painterResource(id = R.mipmap.beaver_icon),
            contentDescription = stringResource(id = R.string.userIcon),
            contentScale = ContentScale.FillWidth
        )
}
@Composable
private fun SmallOtherAccountImages(images: List<Uri>, modifier: Modifier, onClick: () -> Unit){
    if(images.isNotEmpty())
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }.padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            items(images, key = { it }){uri ->
                AsyncImage(
                    modifier = modifier,
                    model = uri,
                    contentDescription = stringResource(id = R.string.userIcon),
                    contentScale = ContentScale.FillWidth
                )
            }
        }
    else
        Image(
            modifier = modifier
                .clickable { onClick() }.padding(5.dp),
            painter = painterResource(id = R.mipmap.beaver_icon),
            contentDescription = stringResource(id = R.string.userIcon),
        )
}