package com.example.bobrarium_v2.ui.pages.account.other_account

import android.net.Uri
import android.util.Log
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bobrarium_v2.R
import com.example.bobrarium_v2.ui.pages.account.AccountViewModel

private const val TAG = "CollapsableImagesList"

@Composable
fun CollapsableImagesList(visible: MutableState<Boolean>, viewModel: AccountViewModel, about: String?){
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
        Log.d(TAG, "update visibility")
        if (targetState) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp)
                ) {
                    BigOtherAccountImages(
                        viewModel.images,
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clickable {
                                visible.value = false
                                Log.d(TAG, "onClick, ${visible.value}")
                            },
                    )
                }

                if(!about.isNullOrBlank())
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp)
                    ) {
                        Text(
                            modifier = Modifier.padding(start = 5.dp, end = 5.dp, top = 5.dp),
                            text = stringResource(id = R.string.about),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth().padding(start = 5.dp, end = 5.dp, bottom = 5.dp),
                            text = about
                        )
                    }
            }
        }
        else
            Row(modifier = Modifier.fillMaxWidth()) {
                SmallOtherAccountImages(
                    viewModel.images,
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    Modifier
                        .weight(1f)
                        .padding(5.dp)
                        .clickable { visible.value = true },
                )
            }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BigOtherAccountImages(images: List<Uri>, modifier: Modifier) {
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
private fun SmallOtherAccountImages(images: List<Uri>, modifier: Modifier, boxModifier: Modifier){
    if(images.isNotEmpty())
        LazyRow(
            modifier = boxModifier,
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
        Box(modifier = boxModifier){
            Image(
                modifier = modifier,
                painter = painterResource(id = R.mipmap.beaver_icon),
                contentDescription = stringResource(id = R.string.userIcon),
            )
        }
}