package com.example.bobrarium_v2.ui.pages.chats.messages

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.bobrarium_v2.R
import com.example.bobrarium_v2.firebase.chat.Message
import com.example.bobrarium_v2.firebase.user.User

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.userMessages(
    key: Any,
    uid: String,
    author: User?,
    messages: List<Message>,
    viewModel: MessagesViewModel,
    onClick: () -> Unit
){
    val isCurrentUser = uid == messages.firstOrNull()?.authorId
    stickyHeader(key = key) {
        Column(modifier = Modifier.fillMaxWidth()) {
            UserChatImage(
                Modifier
                    .size(40.dp, 40.dp)
                    .clickable { onClick() }
                    .clip(RoundedCornerShape(4.dp))
                    .align(if (isCurrentUser) Alignment.End else Alignment.Start),
                author?.fiUri
            )
        }
    }
    itemsIndexed(messages, key = { _, v -> v.id }){ index, message ->
        MessageItem(isCurrentUser, message, author, index == 0)
    }
}

@Composable
fun MessageItem(
    isCurrentUser: Boolean,
    message: Message,
    author: User?,
    isFirst: Boolean
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 45.dp)
    ) {
        if(isCurrentUser)
            Card(
                modifier = Modifier.align(Alignment.End),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Content(true, message, author)
            }
        else {
            if(isFirst)
                Text(
                    text = author?.username?: stringResource(id = R.string.bobeur),
                    style = MaterialTheme.typography.labelMedium
                )
            Card(
                modifier = Modifier.align(Alignment.Start),
            ) {
                Content(false, message, author)
            }
        }
    }
}

@Composable
private fun Content(isCurrentUser: Boolean, message: Message, author: User?){
    Text(text = message.text, modifier = Modifier.padding(5.dp))
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun UserChatImage(modifier: Modifier, uri: Uri?){
    if(uri == null)
        Image(
            modifier = modifier,
            painter = painterResource(id = R.mipmap.beaver_icon),
            contentDescription = stringResource(id = R.string.userIcon),
            contentScale = ContentScale.Crop
        )
    else {
        GlideImage(
            modifier = modifier,
            model = uri,
            contentDescription = stringResource(id = R.string.userIcon),
            contentScale = ContentScale.Crop
        )
    }
}