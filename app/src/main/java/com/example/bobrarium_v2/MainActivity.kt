package com.example.bobrarium_v2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.bobrarium_v2.ui.Drawer
import com.example.bobrarium_v2.ui.theme.Bobrarium_v2Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
//            ProvideWindowInsets {
                val navController = rememberNavController()
                Bobrarium_v2Theme {
                    Drawer(navController)
                }
            }
//        }
    }
}