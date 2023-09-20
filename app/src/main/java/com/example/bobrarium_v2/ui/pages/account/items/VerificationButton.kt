package com.example.bobrarium_v2.ui.pages.account.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bobrarium_v2.R
import com.example.bobrarium_v2.ui.Screen
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun VerificationButton(navController: NavHostController, user: FirebaseUser?, update: () -> Unit) {
    if(user == null){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = { navController.navigate(Screen.SignUp.route) }
            ) {
                Text(stringResource(id = R.string.signUp))
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = { navController.navigate(Screen.SignIn.route) }
            ) {
                Text(text = stringResource(id = R.string.signIn))
            }
        }
    }
    else{
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            onClick = {
                Firebase.auth.signOut()
                update()
            }
        ) {
            Text(stringResource(id = R.string.signOut))
        }
    }
}