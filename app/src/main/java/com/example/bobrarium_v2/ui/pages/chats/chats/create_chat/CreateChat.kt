package com.example.bobrarium_v2.ui.pages.chats.chats.create_chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.bobrarium_v2.CustomState
import com.example.bobrarium_v2.R
import com.example.bobrarium_v2.getNameWithExtension
import com.example.bobrarium_v2.pickImagesOnly

@Composable
fun CreateChat(
    navController: NavHostController,
    chatName: String,
    viewModel: CreateChatViewModel = hiltViewModel()
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
    ){
        val context = LocalContext.current

        val name = remember { mutableStateOf(chatName) }
        val about = remember { mutableStateOf("") }
        val uri = remember { mutableStateOf<Uri?>(null) }

        val nameError = remember { mutableStateOf(false) }

        val showDialog = remember { mutableStateOf(false) }

        val state by viewModel.createState.collectAsState(initial = CustomState())

        if (state.isLoading){
            LinearProgressIndicator(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp))
        }

        if(state.isSuccess != null || state.isError != null){
            showDialog.value = true
        }

        if(showDialog.value){
            val onDismiss = { showDialog.value = false }
            if(state.isSuccess != null)
                Success(state.isSuccess!!, navController, viewModel, onDismiss)
            else Failure(state.isError, navController, onDismiss)
        }

        EditChatImage(uri, !state.isLoading)

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = name.value,
            onValueChange = { name.value = it },
            singleLine = true,
            label = { Text(text = stringResource(id = R.string.chatName)) },
            isError = nameError.value,
            enabled = !state.isLoading
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = about.value,
            onValueChange = { about.value = it },
            label = { Text(text = stringResource(id = R.string.about)) },
            enabled = !state.isLoading
        )
        
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            onClick = {
                if(name.value.isBlank()){
                    nameError.value = true
                }
                else{
                    val imageName = uri.value?.getNameWithExtension(context)
                    viewModel.createChat(name.value, about.value, imageName, uri.value)
                }
            }
        ) {
            Text(text = stringResource(id = R.string.create))
        }


    }
}

@Composable
fun EditChatImage(uri: MutableState<Uri?>, enabled: Boolean){
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()){
            uri.value = it
        }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ){
        ChatImage(uri.value)
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = { launcher.pickImagesOnly() },
            enabled = enabled
        ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(id = R.string.pickImage))
        }
    }
}
@Composable
fun ChatImage(uri: Uri?){
    if (uri == null)
        Image(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
            painter = painterResource(id = R.mipmap.beaver_icon),
            contentDescription = stringResource(id = R.string.chatIcon),
            contentScale = ContentScale.FillWidth
        )
    else
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
            model = uri,
            contentDescription = stringResource(id = R.string.chatIcon),
            contentScale = ContentScale.FillWidth
        )
}