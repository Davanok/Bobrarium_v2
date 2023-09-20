package com.example.bobrarium_v2.ui.pages.chats.chats.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.bobrarium_v2.R
import com.example.bobrarium_v2.firebase.chat.Chat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun ChatPreview(chat: Chat, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(5.dp)) {
            if(chat.uri != null)
                GlideImage(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .aspectRatio(1f),
                    model = chat.uri,
                    contentDescription = stringResource(id = R.string.chatIcon),
                    contentScale = ContentScale.Crop
                )
            else
                Image(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp)),
                    painter = painterResource(id = R.mipmap.beaver_icon),
                    contentDescription = stringResource(id = R.string.chatIcon),
                    contentScale = ContentScale.FillHeight
                )
            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                Text(
                    text = chat.name?: stringResource(id = R.string.undefined),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}