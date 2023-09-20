package com.example.bobrarium_v2.ui.pages.account

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.bobrarium_v2.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPasswordDialog(
    user: FirebaseUser,
    dismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = dismiss
    ) {
        val context = LocalContext.current

        val oldPassword = remember { mutableStateOf("") }
        val newPassword = remember { mutableStateOf("") }
        val repeatPassword = remember { mutableStateOf("") }

        val oldPassErr = remember { mutableStateOf(false) }
        val newPassErr = remember { mutableStateOf(false) }
        val repeatPassErr = remember { mutableStateOf(false) }

        PasswordTextField(stringResource(id = R.string.oldPassword), oldPassErr.value) {
            oldPassword.value = it
            oldPassErr.value = false
        }
        PasswordTextField(stringResource(id = R.string.newPassword), newPassErr.value){
            newPassword.value = it
            newPassErr.value = false
        }
        PasswordTextField(stringResource(id = R.string.repeatPassword), repeatPassErr.value){
            repeatPassword.value = it
            repeatPassErr.value = false
        }

        Button(
            modifier = Modifier.fillMaxWidth().padding(5.dp),
            onClick = {
                if(newPassword.value.length < 6){
                    Toast.makeText(context, R.string.passwordTooShort, Toast.LENGTH_SHORT).show()
                    newPassErr.value = true
                }
                else if(newPassword.value == repeatPassword.value){
                    val credential = EmailAuthProvider.getCredential(user.email?: "", oldPassword.value)
                    user.reauthenticate(credential).addOnSuccessListener {
                        user.updatePassword(newPassword.value)
                        Toast.makeText(context, R.string.successUpdatePassword, Toast.LENGTH_SHORT).show()
                        dismiss()
                    }.addOnFailureListener {
                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                        oldPassErr.value = true
                        newPassErr.value = true
                    }
                }
                else{
                    Toast.makeText(context, R.string.passwordMismatch, Toast.LENGTH_SHORT).show()
                    repeatPassErr.value = true
                }
            }
        ) {
            Text(text = stringResource(id = R.string.updatePassword))
        }
    }
}

@Composable
fun PasswordTextField(
    label: String,
    isError: Boolean,
    update: (String) -> Unit
){
    val password = remember { mutableStateOf("") }
    val showPassword = remember { mutableStateOf(false) }

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth().padding(5.dp),
        value = password.value,
        onValueChange = {
            password.value = it
            update(it)
                        },
        leadingIcon = { Icon(imageVector = Icons.Default.Lock, label) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password
        ),
        visualTransformation = if(showPassword.value) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            if(showPassword.value)
                Icon(
                    modifier = Modifier.clickable { showPassword.value = false },
                    imageVector = ImageVector.vectorResource(id = R.drawable.outline_visibility_off_24),
                    contentDescription = stringResource(id = R.string.hidePassword)
                )
            else
                Icon(
                    modifier = Modifier.clickable { showPassword.value = true },
                    imageVector = ImageVector.vectorResource(id = R.drawable.outline_visibility_24),
                    contentDescription = stringResource(id = R.string.showPassword)
                )
        },
        singleLine = true,
        isError = isError
    )
}