package com.example.bobrarium_v2.ui.pages.account.items

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import com.example.bobrarium_v2.R
import com.example.bobrarium_v2.getNameWithExtension
import com.example.bobrarium_v2.pickImagesOnly
import com.example.bobrarium_v2.pickVisualMedia
import com.example.bobrarium_v2.ui.pages.account.AccountViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountImage(
    fi: String?,
    uid: String,
    viewModel: AccountViewModel
) {
    val context = LocalContext.current

    val favouriteImage = remember { mutableStateOf(fi?:"") }
    val launcher =
        pickVisualMedia{uri ->
            if(uri == null) return@pickVisualMedia

            val filename = uri.getNameWithExtension(context)?: return@pickVisualMedia
            viewModel.addImage(uri, filename, uid)
        }
    Box(
        modifier = Modifier.fillMaxWidth()
    ){
        if(viewModel.images.isEmpty())
            Image(
                modifier = Modifier
                    .padding(10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .fillMaxWidth(),
                painter = painterResource(id = R.mipmap.beaver_icon),
                contentDescription = stringResource(id = R.string.userIcon),
                contentScale = ContentScale.FillWidth
            )
        else
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                if(viewModel.images.isNotEmpty()) {
                    val state = rememberPagerState { viewModel.images.size }
                    HorizontalPager(
                        modifier = Modifier.fillMaxWidth(),
                        state = state
                    ) { position ->
                        val uri = viewModel.images[position]
                        val isFavourite = uri.getNameWithExtension(context) == favouriteImage.value
                        LoadedImage(
                            uri,
                            isFavourite,
                            uid,
                            viewModel,
                            { viewModel.images.remove(it) },
                            { favouriteImage.value = it })
                    }
                    Row(
                        Modifier
                            .align(Alignment.CenterHorizontally),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(state.pageCount){
                            val color =
                                if (state.currentPage == it) MaterialTheme.colorScheme.primary
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
                        modifier = Modifier
                            .padding(10.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        painter = painterResource(id = R.mipmap.beaver_icon),
                        contentDescription = stringResource(id = R.string.userIcon),
                        contentScale = ContentScale.FillWidth
                    )
            }
        IconButton(
            onClick = { launcher.pickImagesOnly() }
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(id = R.string.addImage)
            )
        }
    }
}

@Composable
fun LoadedImage(
    uri: Uri,
    isFavourite: Boolean,
    uid: String,
    viewModel: AccountViewModel,
    onDelete: (Uri) -> Unit,
    onFavourite: (String) -> Unit
){
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ){
        val context = LocalContext.current
        AsyncImage(
            modifier = Modifier
                .padding(start = 10.dp, end = 10.dp, top = 10.dp)
                .clip(RoundedCornerShape(16.dp))
                .fillMaxWidth()
                .aspectRatio(1f),
            model = uri,
            contentDescription = stringResource(id = R.string.userIcon),
            contentScale = ContentScale.FillWidth
        )
        IconButton(
            onClick = {
                val filename = uri.getNameWithExtension(context)
                if(filename != null) {
                    viewModel.makeImageFavourite(uid, filename)
                    onFavourite(filename)
                }
                      },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            if(isFavourite)
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = stringResource(id = R.string.makeUnFavourite)
                )
            else
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(id = R.string.makeFavourite)
                )
        }
        IconButton(
            onClick = {
                val filename = uri.getNameWithExtension(context)
                if(filename != null) viewModel.deleteImage(uid, filename) { onDelete(uri) }
            },
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(id = R.string.deleteImage))
        }
    }
}

