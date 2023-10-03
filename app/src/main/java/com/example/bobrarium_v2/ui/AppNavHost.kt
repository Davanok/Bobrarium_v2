package com.example.bobrarium_v2.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bobrarium_v2.AppViewModel
import com.example.bobrarium_v2.ui.pages.Settings
import com.example.bobrarium_v2.ui.pages.account.Account
import com.example.bobrarium_v2.ui.pages.account.auth.sign_in.SignInScreen
import com.example.bobrarium_v2.ui.pages.account.auth.sign_up.SignUpScreen
import com.example.bobrarium_v2.ui.pages.account.other_account.OtherAccount
import com.example.bobrarium_v2.ui.pages.chats.chats.chatSettings.ChatInfo
import com.example.bobrarium_v2.ui.pages.chats.chats.chatSettings.ChatSettings
import com.example.bobrarium_v2.ui.pages.chats.chats.create_chat.CreateChat
import com.example.bobrarium_v2.ui.pages.chats.chats.list.ChatsList
import com.example.bobrarium_v2.ui.pages.chats.messages.Chat
import com.example.bobrarium_v2.ui.pages.chats.messages.PrivateChat

@Composable
fun AppNavHost(
    modifier: Modifier,
    navController: NavHostController,
    viewModel: AppViewModel
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Screen.ChatsList.route
    ){
        composable(Screen.ChatsList.route) { ChatsList(navController, viewModel) }
        composable(Screen.Account.route) { Account(navController, viewModel) }
        composable(Screen.Settings.route) { Settings(navController) }

        composable(Screen.SignIn.route) { SignInScreen(navController) }
        composable(Screen.SignUp.route) { SignUpScreen(navController) }

        composable(
            Screen.Chat().route,
            Screen.Chat().arguments
        ) { Chat(navController, it.getString("chatId")?: "", viewModel) }
        composable(
            Screen.CreateChat().route,
            Screen.CreateChat().arguments
        ){ CreateChat(navController, it.getString("chatName")?: "") }
        composable(
            Screen.ChatSettings().route,
            Screen.ChatSettings().arguments
        ){ ChatSettings(navController, it.getString("chatId")?: "", viewModel) }
        composable(
            Screen.ChatInfo().route,
            Screen.ChatInfo().arguments
        ){ ChatInfo(navController, it.getString("chatId")?: "", viewModel) }
        composable(
            Screen.PrivateChat().route,
            Screen.PrivateChat().arguments
        ){ PrivateChat(navController, it.getString("userId")?: "", viewModel) }

        composable(
            Screen.OtherAccount().route,
            Screen.OtherAccount().arguments
        ){ OtherAccount(navController, it.getString("userId")?: "", viewModel) }

    }
}

private fun NavBackStackEntry.getString(key: String) = arguments?.getString(key)