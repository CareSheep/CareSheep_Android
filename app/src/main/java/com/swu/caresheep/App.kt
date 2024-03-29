package com.swu.caresheep

import android.app.Application
import android.content.Context

class App : Application() {
    companion object {

        lateinit var instance: App

        fun getApplicationContext(): Context {
            return instance.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}