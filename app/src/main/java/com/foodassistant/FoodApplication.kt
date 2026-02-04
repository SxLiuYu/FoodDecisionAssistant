package com.foodassistant

import android.app.Application
import android.util.Log

/**
 * 应用入口
 */
class FoodApplication : Application() {
    
    companion object {
        private const val TAG = "FoodApplication"
        lateinit var instance: FoodApplication
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d(TAG, "Application started")
        
        // 初始化全局配置
        initDebugConfig()
    }
    
    private fun initDebugConfig() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Debug mode enabled")
        }
    }
}
