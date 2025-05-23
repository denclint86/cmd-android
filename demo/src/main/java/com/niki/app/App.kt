package com.niki.app

import android.app.Application
import com.google.android.material.color.DynamicColors

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)
////        Logger.startLogger(this, LogLevel.VERBOSE)
//
//        // 尝试直接用 root 启动无障碍
//        "pm grant $packageName android.permission.SYSTEM_ALERT_WINDOW"
//        "pm grant $packageName android.permission.WRITE_SECURE_SETTINGS"
////        "settings put secure enabled_accessibility_services $packageName/$accessibilityServiceClassPath"
//        "settings put secure accessibility_enabled 1"
//        "settings get secure enabled_accessibility_services"
    }
}