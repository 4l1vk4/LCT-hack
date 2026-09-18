package ru.finpet.kids

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FinPetApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
