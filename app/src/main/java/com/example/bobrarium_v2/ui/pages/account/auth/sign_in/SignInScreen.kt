package com.example.bobrarium_v2.ui.pages.account.auth.sign_in

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bobrarium_v2.CustomState
import com.example.bobrarium_v2.R
import com.example.bobrarium_v2.ui.Screen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
    navController: NavHostController,
    viewModel: SignInViewModel = hiltViewModel()
) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val state = viewModel.signInState.collectAsState(initial = CustomState<String>())

    val emailError = remember { mutableStateOf(false) }
    val passwordError = remember { mutableStateOf(false) }

    val showPassword = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            value = viewModel.email.value,
            onValueChange = { viewModel.email.value = it },
            leadingIcon = { Icon(imageVector = Icons.Default.Email, stringResource(id = R.string.email)) },
            label = { Text(stringResource(id = R.string.email)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            isError = emailError.value,
            singleLine = true
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            value = viewModel.password.value,
            onValueChange = { viewModel.password.value = it },
            leadingIcon = { Icon(imageVector = Icons.Default.Lock, stringResource(id = R.string.password)) },
            label = { Text(stringResource(id = R.string.password)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            isError = passwordError.value,
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
            singleLine = true
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            onClick = {
                scope.launch {
                    if(viewModel.email.value.isNotBlank() && viewModel.password.value.length >= 6)
                        viewModel.loginUser()
                    if(viewModel.email.value.isBlank()) emailError.value = true
                    if(viewModel.password.value.length < 6) passwordError.value = true
                }
            }
        ) {
            Text(text = stringResource(id = R.string.signIn))
        }
        if(state.value.isLoading){
            CircularProgressIndicator()
        }
        TextButton(onClick = { navController.navigate(Screen.SignUp.route) }) {
            Text(text = AnnotatedString(stringResource(id = R.string.signUp)))
        }
        val text = stringResource(id = R.string.sentResetPassword, viewModel.email.value)
        TextButton(
            onClick = {
                if(viewModel.email.value.isBlank()) {
                    emailError.value = true
                    Toast.makeText(context, R.string.emailIsBlank, Toast.LENGTH_SHORT).show()
                }
                else Firebase.auth.sendPasswordResetEmail(viewModel.email.value).addOnSuccessListener {
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text(text = stringResource(id = R.string.forgotPassword))
        }
        LaunchedEffect(key1 = state.value.isSuccess){
            scope.launch {
                if(!state.value.isSuccess.isNullOrBlank()){
                    val success = state.value.isSuccess
                    Toast.makeText(context, "$success, email is done", Toast.LENGTH_SHORT).show()
                    navController.navigate(Screen.Account.route){
                        popUpTo(Screen.ChatsList.route)
                    }
                }
            }
        }
        LaunchedEffect(key1 = state.value.isError){
            scope.launch {
                if(!state.value.isError.isNullOrBlank()){
                    val error = state.value.isError
                    Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show()
                    checkEmailVerification(context)
                }
            }
        }
    }
}

fun checkEmailVerification(context: Context){
    val user = Firebase.auth.currentUser
    if(user?.isEmailVerified == true) Toast.makeText(context, "success!", Toast.LENGTH_SHORT).show()
    else Toast.makeText(context, "fail", Toast.LENGTH_SHORT).show()
}
