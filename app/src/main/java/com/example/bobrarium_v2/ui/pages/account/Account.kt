package com.example.bobrarium_v2.ui.pages.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bobrarium_v2.AppViewModel
import com.example.bobrarium_v2.CustomState
import com.example.bobrarium_v2.R
import com.example.bobrarium_v2.ui.pages.account.items.AboutTextField
import com.example.bobrarium_v2.ui.pages.account.items.AccountImage
import com.example.bobrarium_v2.ui.pages.account.items.EmailTextField
import com.example.bobrarium_v2.ui.pages.account.items.UsernameTextField
import com.example.bobrarium_v2.ui.pages.account.items.VerificationButton
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun Account(
    navController: NavHostController,
    appViewModel: AppViewModel,
    viewModel: AccountViewModel = hiltViewModel()
) {

    val user = remember { mutableStateOf(Firebase.auth.currentUser) }
    val uid = user.value?.uid
    if(uid != null) {
        viewModel.loadUser(uid)
        viewModel.loadImages(uid)
    }
    Content(navController, user.value, appViewModel, viewModel){
        user.value = null
    }
}

@Composable
private fun Content(
    navController: NavHostController,
    user: FirebaseUser?,
    appViewModel: AppViewModel,
    viewModel: AccountViewModel,
    onUpdate: () -> Unit
){
    val context = LocalContext.current

    val showDialog = remember { mutableStateOf(false) }
    val showEditPasswordDialog = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val isLoading by viewModel.isLoading
        if(isLoading){
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }
        if(user != null){

            val state by viewModel.loadUserState.collectAsState(initial = CustomState())

            if (state.isSuccess != null) {
                val databaseUser = state.isSuccess!!

                appViewModel.appBarTitle.value = databaseUser.username
                appViewModel.currentUser.value = databaseUser

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                ) {
                    AccountImage(databaseUser.favouriteImage, user.uid, viewModel)
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        UsernameTextField(R.string.username, databaseUser.username) { viewModel.updateUsername(user.uid, it) }
                        AboutTextField(databaseUser.about) { viewModel.updateAbout(user.uid, it) }

                        EmailTextField(user) { showDialog.value = true }
                        com.example.bobrarium_v2.ui.pages.account.items.PasswordTextField {
                            showEditPasswordDialog.value = true
                        }
                    }
                }
            }
        }
        VerificationButton(navController, user, onUpdate)
    }

    if(showDialog.value){
        EmailVerificationDialog(user, context){ showDialog.value = false }
    }

    if(showEditPasswordDialog.value && user != null){
        EditPasswordDialog(user){showEditPasswordDialog.value = false}
    }
}