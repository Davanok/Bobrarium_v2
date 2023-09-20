package com.example.bobrarium_v2.ui.pages.account.items

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bobrarium_v2.R

@Composable
fun AboutTextField(
    a: String?,
    onUpdate: (String) -> Unit
) {
    val oldAboutText = remember { mutableStateOf("") }
    val editAboutText = remember { mutableStateOf(false) }
    val aboutText = remember { mutableStateOf(a?: "") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = aboutText.value,
            onValueChange = { aboutText.value = it },
            enabled = editAboutText.value,
            label = { Text(text = stringResource(id = R.string.about))},
            singleLine = false
        )
        IconButton(
            onClick = {
                if(editAboutText.value) onUpdate(aboutText.value)
                oldAboutText.value = aboutText.value
                editAboutText.value = !editAboutText.value
            }
        ) {
            if(editAboutText.value)
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(id = R.string.confirm)
                )
            else
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(id = R.string.edit)
                )
        }
        if(editAboutText.value){
            IconButton(
                onClick = {
                    editAboutText.value = false
                    aboutText.value = oldAboutText.value
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(id = R.string.cancel)
                )
            }
        }
    }
}