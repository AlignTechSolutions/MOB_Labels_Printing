package com.alignTech.labelsPrinting.core.app

import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.alfayedoficial.kotlinutils.KUPreferences
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BaseApp : MultiDexApplication() {

    @Inject
    lateinit var appPreferences: KUPreferences

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
    }


}