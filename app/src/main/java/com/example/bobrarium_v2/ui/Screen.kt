package com.example.bobrarium_v2.ui

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String, val arguments: List<NamedNavArgument> = emptyList()){
    object ChatsList: Screen("chatsList")
    object Account: Screen("account")
    object Settings: Screen("settings")

    object SignIn: Screen("signIn")
    object SignUp: Screen("signUp")

    class Chat(argument: String = "{chatId}"): Screen(
        "chat/$argument",
        listOf(navArgument("chatId") { type = NavType.StringType } )
    )
    class CreateChat(argument: String = "{chatName}"): Screen(
        "createChat/$argument",
        listOf(navArgument("chatName") { type = NavType.StringType })
    )
    class ChatSettings(argument: String = "{chatId}"): Screen(
        "chatSettings/$argument",
        listOf(navArgument("chatId") { type = NavType.StringType })
    )
    class ChatInfo(argument: String = "{chatId}"): Screen(
        "chatInfo/$argument",
        listOf(navArgument("chatId") { type = NavType.StringType })
    )
    class PrivateChat(argument: String = "{userId}"): Screen(
        "privateChat/$argument",
        listOf(navArgument("userId") { type = NavType.StringType })
    )

    class OtherAccount(argument: String = "{userId}"): Screen(
        "otherAccount/$argument",
        listOf(navArgument("userId") { type = NavType.StringType })
    )
}
