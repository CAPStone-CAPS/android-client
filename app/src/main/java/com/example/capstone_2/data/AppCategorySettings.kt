package com.example.capstone_2.data

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import java.time.LocalDateTime
import java.time.ZoneId

// 앱별로 사용자가 지정한 카테고리를 Int로 저장한다.
// 카테고리: 0 = 놀기(기본), 1 = 공부.
data class AppCategorySettings (val username: String) {
    val SETTING_LEISURE = 0
    val SETTING_PRODUCTIVE = 1

    private var categoryMap : MutableMap<String, Int> = mutableMapOf<String, Int>()

    fun addApp(appName: String) {
        if(categoryMap.containsKey(appName) == false){
            categoryMap.put(appName, 0)
        }
    }

    fun setAppCategory(appName: String, newCategory: Int) {
        categoryMap.put(appName, newCategory)
    }

    fun getAppCategory(appName: String): Int {
        return categoryMap.getValue(appName)
    }

    fun getAllAppCategory(): Map<String, Int> {
        return categoryMap.toMap()
    }

    fun getAppNameSet(): Set<String> {
        return categoryMap.keys
    }

    fun getAppListFromUsageStats(context: Context) {
        val myUsageStatsManager: UsageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val appNameSet: MutableSet<String> = mutableSetOf<String>()

<<<<<<< HEAD
        val millisNow = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
            .atZone(ZoneId.of("Asia/Seoul"))
=======
        val millisNow = LocalDateTime.now()
            .atZone(ZoneId.systemDefault())
>>>>>>> 751dd7113797efac61f43c75ac2dc4796661c493
            .toInstant().toEpochMilli()

        val millisYesterday : Long = ((millisNow - 86400000) / 86400000) * 86400000

        // 어제부터 지금까지 Stats를 불러오기?
        val usageList = myUsageStatsManager.queryAndAggregateUsageStats(millisYesterday, millisNow)
        val packageNameSet: MutableSet<String> = usageList.keys

        packageNameSet.forEach {
            addApp(it)
            Log.d("APP-CAT-SETTING", "새로운 앱 추가: ${it}")
        }
    }
}