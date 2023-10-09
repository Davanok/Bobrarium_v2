package com.example.bobrarium_v2.ui.pages.chats.chats.new_chat_dialog

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bobrarium_v2.R
import com.example.bobrarium_v2.firebase.chat.Chat
import com.example.bobrarium_v2.firebase.user.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogChatItem(chat: Chat, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemImage(uri = chat.uri)
            Text(text = chat.name?: stringResource(id = R.string.bobeur), Modifier.padding(start = 10.dp))
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogChatItem(user: User, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemImage(uri = user.fiUri)
            Text(text = user.username?: stringResource(id = R.string.bobeur), Modifier.padding(start = 10.dp))
        }
    }
}

@Composable
fun ItemImage(uri: Uri?){
    if (uri == null)
        Image(
            modifier = Modifier
                .fillMaxHeight()
                .clip(RoundedCornerShape(4.dp)),
            painter = painterResource(id = R.mipmap.beaver_icon),
            contentDescription = stringResource(id = R.string.chatIcon),
            contentScale = ContentScale.FillHeight
        )
    else
        AsyncImage(
            modifier = Modifier
                .fillMaxHeight()
                .clip(RoundedCornerShape(10.dp)),
            model = uri,
            contentDescription = stringResource(id = R.string.chatIcon),
            contentScale = ContentScale.FillHeight
        )
}