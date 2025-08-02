package com.example.capstone_2.data

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log

class AppInfoHelper(private val context: Context) {

    private val packageManager = context.packageManager

    // 강력한 메모리 캐싱
    private val nameCache = mutableMapOf<String, String>()
    private val iconCache = mutableMapOf<String, Drawable>()

    fun getAppName(packageName: String): String {
        return nameCache.getOrPut(packageName) {
            try {
                val info = packageManager.getApplicationInfo(packageName, 0)
                val label = packageManager.getApplicationLabel(info).toString()
                Log.d("APP_DEBUG", "앱 이름 가져옴: $packageName → $label")
                label
            } catch (e: Exception) {
                Log.e("APP_DEBUG", "앱 이름 못 가져옴: $packageName", e)
                packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
            }
        }
    }

    fun getAppIcon(packageName: String): Drawable {
        return iconCache.getOrPut(packageName) {
            try {
                val info = packageManager.getApplicationInfo(packageName, 0)
                packageManager.getApplicationIcon(info)
            } catch (e: Exception) {
                packageManager.defaultActivityIcon
            }
        }
    }
}
