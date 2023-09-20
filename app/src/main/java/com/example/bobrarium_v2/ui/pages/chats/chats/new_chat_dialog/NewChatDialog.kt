package com.example.bobrarium_v2.ui.pages.chats.chats.new_chat_dialog

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.bobrarium_v2.CustomState
import com.example.bobrarium_v2.R
import com.example.bobrarium_v2.ui.Screen

inline fun <reified T> SnapshotStateList<T>.setElements(elements: Collection<T>){
    this.clear()
    this.addAll(elements)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatDialog(
    navController: NavHostController,
    viewModel: NewChatDialogViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    viewModel.loadChatsList()
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        val text = remember { mutableStateOf("") }
        val isError = remember { mutableStateOf(false) }

        val state by viewModel.chatsState.collectAsState(initial = CustomState())

        val context = LocalContext.current

        val onValueChanged: (String) -> Unit = {
            text.value = it
            if (state.isSuccess != null){
                if(it.isBlank()) viewModel.filteredChats.setElements(state.isSuccess!!)
                else viewModel.filteredChats.setElements(
                    state.isSuccess!!.filter { chat -> chat.name?.lowercase()?.contains(it.lowercase()) != false }
                )
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
            trailingIcon = {
                IconButton(
                    onClick = {
                        onDismiss()
                        if(text.value.isBlank())
                            isError.value = true
                        else if(state.isSuccess?.map { it.name }?.contains(text.value) != false){
                            isError.value = true
                            Toast.makeText(context, R.string.nameTaken, Toast.LENGTH_SHORT).show()
                        }
                        else
                            navController.navigate(Screen.CreateChat(text.value).route)
                    }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.newChat))
                }
            },
            isError = isError.value
        )
        if (state.isLoading){
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        else if (state.isError != null){
            Icon(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .align(Alignment.CenterHorizontally),
                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_error_outline_24),
                contentDescription = stringResource(id = R.string.errorChatsLoading)
            )
            Text(text = stringResource(id = R.string.errorChatsLoading))
            Text(text = state.isError.toString())
        }
        else if (state.isSuccess != null){
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
                            { chatId ->
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

    }
}