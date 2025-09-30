package com.dlx.smartalarm.demo

import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化全局上下文
        appContext = applicationContext
    }
}
