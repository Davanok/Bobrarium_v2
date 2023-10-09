package com.example.bobrarium_v2.ui.pages.chats.chats.new_chat_dialog

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LeadingIconTab
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bobrarium_v2.R
import com.example.bobrarium_v2.Simple
import com.example.bobrarium_v2.ui.Screen
import kotlinx.coroutines.launch

inline fun <reified T> SnapshotStateList<T>.setElements(elements: Collection<T>){
    this.clear()
    this.addAll(elements)
}
inline fun <reified T> MutableList<T>.setElements(elements: Collection<T>){
    this.clear()
    this.addAll(elements)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NewChatDialog(
    navController: NavHostController,
    viewModel: NewChatDialogViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val chatsState by viewModel.chatsState
    val usersState by viewModel.usersState
    if (chatsState == null) viewModel.loadChatsList()
    if (usersState == null) viewModel.loadUsersList()

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        val scope = rememberCoroutineScope()
        val pagerState = rememberPagerState { 2 }
        val text = remember { mutableStateOf("") }
        val isError = remember { mutableStateOf(false) }
        val context = LocalContext.current

        val onValueChanged: (String) -> Unit = {
            text.value = it
            if (chatsState is Simple.Success){
                if(it.isBlank()) viewModel.filteredChats.setElements(viewModel.chats)
                else viewModel.filteredChats.setElements(
                    viewModel.chats.filter { chat -> chat.name?.lowercase()?.contains(it.lowercase()) != false }
                )
            }
            if (usersState is Simple.Success){
                if(it.isBlank()) viewModel.filteredUsers.setElements(viewModel.users)
                else viewModel.filteredChats.setElements(
                    viewModel.chats.filter { chat -> chat.name?.lowercase()?.contains(it.lowercase()) != false }
                )
            }
        }
        val trailingIcon = @Composable {
            IconButton(
                onClick = {
                    onDismiss()
                    if(text.value.isBlank())
                        isError.value = true
                    else if(viewModel.chats.map { it.name }.contains(text.value)){
                        isError.value = true
                        Toast.makeText(context, R.string.nameTaken, Toast.LENGTH_SHORT).show()
                    }
                    else
                        navController.navigate(Screen.CreateChat(text.value).route)
                }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.newChat))
            }
        }

        if(viewModel.isLoading.value) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())


        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            value = text.value,
            onValueChange = onValueChanged,
            singleLine = true,
            label = { Text(text = stringResource(id = R.string.chatName)) },
            trailingIcon = if (pagerState.currentPage == 0) trailingIcon else null,
            isError = isError.value
        )
        if (chatsState is Simple.Fail || usersState is Simple.Fail){
            Icon(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .align(Alignment.CenterHorizontally),
                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_error_outline_24),
                contentDescription = stringResource(id = R.string.errorChatsLoading)
            )
            Text(text = stringResource(id = R.string.errorChatsLoading))
        }
        else if (chatsState is Simple.Success){
            val tabs = listOf(stringResource(R.string.chats), stringResource(R.string.users))
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, tab ->
                    LeadingIconTab(
                        selected = index == pagerState.currentPage,
                        onClick = { scope.launch { pagerState.scrollToPage(index) } },
                        text = { Text(text = tab) },
                        icon = {
                            Icon(
                                imageVector =
                                if (index == 0) ImageVector.vectorResource(R.drawable.baseline_chat_24)
                                else Icons.Default.AccountCircle,
                                contentDescription = tab
                            )
                        }
                    )
                }
            }
            HorizontalPager(
                modifier = Modifier
                    .fillMaxWidth(),
                state = pagerState
            ) {
                if (it == 0) Chats(
                    navController,
                    viewModel,
                    onDismiss
                )
                else Users(
                    navController,
                    viewModel,
                    onDismiss
                )
            }
        }

    }
}
@Composable
private fun Chats(
    navController: NavHostController,
    viewModel: NewChatDialogViewModel,
    onDismiss: () -> Unit
){
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        items(
            viewModel.filteredChats,
            key = { it.id }
        ){ chat ->
            DialogChatItem(chat) {
                viewModel.addChatForUser(
                    chat,
                    { _ ->
                        onDismiss()
                    }, { message ->
                        Toast.makeText(context, "Error:\n$message", Toast.LENGTH_SHORT).show()
                    }
                )
                navController.navigate(Screen.Chat(chat.id).route)
            }
        }
    }
}
@Composable
private fun Users(
    navController: NavHostController,
    viewModel: NewChatDialogViewModel,
    onDismiss: () -> Unit
){
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        items(
            viewModel.filteredUsers,
            key = { it.uid }
        ){ user ->
            DialogChatItem(user) {
                onDismiss()
                navController.navigate(Screen.OtherAccount(user.uid).route)
            }
        }
    }
}