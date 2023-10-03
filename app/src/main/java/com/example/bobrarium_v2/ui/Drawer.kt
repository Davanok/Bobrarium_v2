package com.example.bobrarium_v2.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.bobrarium_v2.AppViewModel
import com.example.bobrarium_v2.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

data class DrawerItem(
    val icon: ImageVector,
    val title: String,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Drawer(navController: NavHostController, viewModel: AppViewModel = viewModel()){

    val items = listOf(
        DrawerItem(
            ImageVector.vectorResource(id = R.drawable.baseline_chat_24),
            Screen.ChatsList.route,
            stringResource(id = R.string.chats_title)
        ),
        DrawerItem(
            Icons.Default.AccountBox,
            Screen.Account.route,
            stringResource(id = R.string.account_title)
        ),
        DrawerItem(
            Icons.Default.Settings,
            Screen.Settings.route,
            stringResource(id = R.string.settings_title)
        )
    )

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val selectedItem = remember{ mutableStateOf(items[0]) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Image(
                    painter = painterResource(id = R.mipmap.beaver),
                    contentDescription = stringResource(id = R.string.app_name)
                )
                Spacer(modifier = Modifier.height(10.dp))
                items.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedItem.value == item,
                        onClick = {
                            scope.launch {
                                selectedItem.value = item
                                drawerState.close()
                                navController.navigate(item.title)
                            }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = viewModel.appBarTitle.value?: stringResource(id = R.string.app_name))
                            },
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(id = R.string.menu),
                            modifier = Modifier.clickable {
                                scope.launch { drawerState.open() }
                            }
                        )
                    },
                    actions = {
                        val backStack by navController.currentBackStackEntryAsState()
                        when(backStack?.destination?.route){
                            Screen.Chat().route -> {
                                IconButton(onClick = {
                                    val chat by viewModel.chat
                                    if(chat?.id != null)
                                        navController.navigate(Screen.ChatInfo(chat?.id!!).route)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = stringResource(id = R.string.editChat)
                                    )
                                }
                            }
                            Screen.PrivateChat().route -> {
                                IconButton(onClick = {
                                    val chat by viewModel.chat
                                    if(chat?.isPrivate?.uid != null)
                                        navController.navigate(Screen.OtherAccount(chat?.isPrivate?.uid!!).route)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = stringResource(id = R.string.editChat)
                                    )
                                }
                            }
                            Screen.ChatInfo().route -> {
                                val chat by viewModel.chat
                                if(Firebase.auth.uid != null && chat?.admins?.contains(Firebase.auth.uid) == true)
                                    IconButton(onClick = {
                                        navController.navigate(Screen.ChatSettings(chat!!.id).route)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = stringResource(id = R.string.editChat)
                                        )
                                    }
                            }
                            Screen.ChatsList.route ->
                                viewModel.appBarTitle.value = stringResource(id = R.string.app_name)
                            else -> { viewModel.appBarTitle.value = null }
                        }
                    }
                )
            },
        ) { padding ->
            AppNavHost(Modifier.padding(padding), navController, viewModel)
        }
    }
}