package com.example.bobrarium_v2.ui.pages.chats.chats.create_chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.core.text.isDigitsOnly
import androidx.navigation.NavHostController
import com.example.bobrarium_v2.R
import com.example.bobrarium_v2.firebase.chat.Chat
import com.example.bobrarium_v2.ui.Screen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch


@Composable
fun Success(chat: Chat, navController: NavHostController, viewModel: CreateChatViewModel, onDismiss: () -> Unit){
    val scope = rememberCoroutineScope()
    LaunchedEffect(key1 = chat.id){
        scope.launch {
            viewModel.addChatForUser(Firebase.auth.uid, chat.id)
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    navController.navigate(Screen.Chat(chat.id).route) {
                        popUpTo(Screen.ChatsList.route)
                } }
            ) {
                Text(text = stringResource(id = R.string.go))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    navController.popBackStack(Screen.ChatsList.route, false)
                }
            ) {
                Text(text = stringResource(id = R.string.back))
            }
        },
        icon = {
            Image(
                modifier = Modifier.fillMaxWidth(),
                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_check_circle_outline_24),
                contentDescription = stringResource(id = R.string.success),
                contentScale = ContentScale.FillWidth,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
        },
        title = { Text(text = stringResource(id = R.string.success)) },
        text = { Text(text = stringResource(id = R.string.successCreateChat)) }
    )
}
@Composable
fun Failure(message: String?, navController: NavHostController, onDismiss: () -> Unit){
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    navController.popBackStack(Screen.ChatsList.route, false)
                }
            ) {
                Text(text = stringResource(id = R.string.back))
            }
        },
        icon = {
            Image(
                modifier = Modifier.fillMaxWidth(),
                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_error_outline_24),
                contentDescription = stringResource(id = R.string.success),
                contentScale = ContentScale.FillWidth,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
        },
        title = { Text(text = stringResource(id = R.string.failure)) },
        text = {
            if(!message.isNullOrBlank()) {
                if (message.isDigitsOnly()) Text(text = stringResource(id = message.toInt()))
                else Text(text = message.toString())
            }
        }
    )
}