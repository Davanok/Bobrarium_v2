package com.example.bobrarium_v2

import android.app.Application
import com.example.bobrarium_v2.database.AppDatabase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application(){
    val appDatabase by lazy { AppDatabase.create(this) }
}