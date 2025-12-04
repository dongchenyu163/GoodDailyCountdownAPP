package com.dlx.smartalarm.demo

import android.app.Application
import com.dlx.smartalarm.demo.core.platform.getPlatform

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化平台，这将设置 appContext
        getPlatform(this)
    }
}
