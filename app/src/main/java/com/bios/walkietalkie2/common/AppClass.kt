package com.bios.walkietalkie2.common

import android.app.Application

class AppClass: Application() {
    companion object {
        private lateinit var app: AppClass
        fun getApp() = requireNotNull(app)
    }

    override fun onCreate() {
        super.onCreate()
        app = this
    }
}