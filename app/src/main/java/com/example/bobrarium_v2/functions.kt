package com.example.bobrarium_v2

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

fun Uri.getExtension(context: Context): String?{
    val type = context.contentResolver.getType(this)
    return type?.takeLastWhile { it != '/' }
}
fun Uri.getNameWithExtension(): String?{
    val result = this.path?.takeLastWhile { it != '/' }?: return null
    if(!result.contains('.')) return null
    return result
}
fun Uri.getNameWithExtension(context: Context): String?{
    var result = this.path?.takeLastWhile { it != '/' }
    if(result?.contains('.') != true)
        result += ".${this.getExtension(context)}"
    return result
}
fun ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>.pickImagesOnly(){
    this.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
}
@Composable
fun pickVisualMedia(result: (Uri?) -> Unit): ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>{
    return rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia(), result)
}
fun <T> Iterable<T>.groupConsecutiveBy(identifier: (T) -> Any) =
    if (!this.any())
        emptyList()
    else this
        .drop(1)
        .fold(mutableListOf(mutableListOf(this.first()))) { groups, t ->
            groups.last().apply {
                if (identifier(last()) == identifier(t)) {
                    add(t)
                } else {
                    groups.add(mutableListOf(t))
                }
            }
            groups
        }
@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}
@Composable
fun LazyListState.isScrollingDown(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex < firstVisibleItemIndex
            } else {
                previousScrollOffset <= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}