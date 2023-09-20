package com.example.bobrarium_v2.ui.pages.account

import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.bobrarium_v2.R
import com.google.firebase.auth.FirebaseUser


@Composable
fun EmailVerificationDialog(
    user: FirebaseUser?,
    context: Context,
    onDismissRequest: () -> Unit
){
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(imageVector = Icons.Default.MailOutline, contentDescription = stringResource(id = R.string.verifyEmailFor))
        },
        title = { Text(text = stringResource(id = R.string.verifyEmail)) },
        text = { Text(text = stringResource(id = R.string.verifyEmailFor)) },
        confirmButton = {
            val text = stringResource(id = R.string.emailSent, user?.email?: "")
            TextButton(
                onClick = {
                    user?.sendEmailVerification()?.addOnCompleteListener {
                        if(it.isSuccessful) Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                        else Toast.makeText(context, R.string.emailCanceled, Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text(text = stringResource(id = R.string.verify))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(text = stringResource(id = R.string.notNow))
            }
        }
    )
}