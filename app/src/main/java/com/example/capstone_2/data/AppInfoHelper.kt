package com.example.capstone_2.data

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log

class AppInfoHelper(private val context: Context) {
    private val packageManager = context.packageManager

    // 늦은 캐싱 적용
    private val appMap: Map<String, android.content.pm.ApplicationInfo> by lazy {
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .associateBy { it.packageName }
    }

    fun getAppName(packageName: String): String {
        return try {
            val info = appMap[packageName] ?: packageManager.getApplicationInfo(packageName, 0)
            val label = packageManager.getApplicationLabel(info).toString()
            Log.d("APP_DEBUG", "앱 이름 가져옴: $packageName → $label")
            label
        } catch (e: Exception) {
            Log.e("APP_DEBUG", "앱 이름 못 가져옴: $packageName", e)
            packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
        }
    }

    fun getAppIcon(packageName: String): Drawable {
        return try {
            val info = appMap[packageName] ?: packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationIcon(info)
        } catch (e: Exception) {
            packageManager.defaultActivityIcon
        }
    }
}
