package com.example.bobrarium_v2.ui.pages.account.items

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.bobrarium_v2.R
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun EmailTextField(
    user: FirebaseUser,
    dialog: () -> Unit
) {
    val editEmail = remember { mutableStateOf(false) }
    val isEmailVerified = remember { mutableStateOf(Firebase.auth.currentUser?.isEmailVerified) }
    val oldEmail = remember { mutableStateOf("") }
    val email = remember { mutableStateOf(user.email?: "") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = email.value,
            enabled = editEmail.value,
            onValueChange = {
                email.value = it
            },
            trailingIcon = getTrailingIcon(email.value, isEmailVerified.value) {
                Log.d("MyLog", Firebase.auth.currentUser?.isEmailVerified.toString())
                isEmailVerified.value = Firebase.auth.currentUser?.isEmailVerified
                dialog()
            },
            label = { Text(text = stringResource(id = R.string.email)) },
            singleLine = true
        )
        IconButton(
            onClick = {
                if (editEmail.value) user.updateEmail(email.value)
                oldEmail.value = email.value
                editEmail.value = !editEmail.value
            }
        ){
            if(editEmail.value)
                Icon(imageVector = Icons.Default.Check, contentDescription = stringResource(id = R.string.confirm))
            else
                Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(id = R.string.editEmail))
        }
        if(editEmail.value){
            IconButton(
                onClick = {
                    editEmail.value = false
                    email.value = oldEmail.value
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
@Composable
fun getTrailingIcon(email: String, isEmailVerified: Boolean?, result: () -> Unit): (@Composable () -> Unit)? {
    if(email.isBlank()) return null
    if(isEmailVerified == true) return {
        val context = LocalContext.current
        Icon(
            Icons.Rounded.CheckCircle,
            stringResource(id = R.string.emailIsVerified),
            Modifier.clickable {
                Toast.makeText(context, R.string.emailIsVerified, Toast.LENGTH_SHORT).show()
            }
        )
    }
    return {
        Icon(
            ImageVector.vectorResource(id = R.drawable.baseline_error_outline_24),
            stringResource(id = R.string.emailIsNotVerified),
            Modifier.clickable {
                result()
            }
        )
    }
}