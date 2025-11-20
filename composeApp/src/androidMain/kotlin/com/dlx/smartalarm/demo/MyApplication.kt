package com.dlx.smartalarm.demo

import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化平台，这将设置 appContext
        getPlatform(this)
    }
}
