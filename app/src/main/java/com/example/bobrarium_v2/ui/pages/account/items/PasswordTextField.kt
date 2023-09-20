package com.example.bobrarium_v2.ui.pages.account.items

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.bobrarium_v2.R

@Composable
fun PasswordTextField(showDialog: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 5.dp),
            value = "lorem ipsum",
            onValueChange = {  },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            visualTransformation = PasswordVisualTransformation(),
            label = { Text(text = stringResource(id = R.string.password))},
            enabled = false,
            singleLine = true
        )
        IconButton(
            onClick = {
                showDialog()
            }
        ) {
            Icon(
                imageVector = Icons.Default.Edit, 
                contentDescription = stringResource(id = R.string.editPassword))
        }
    }
}