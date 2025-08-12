package com.example.capstone_2.data

// 앱별로 사용자가 지정한 카테고리를 Int로 저장한다.
// 카테고리: 0 = 놀기(기본), 1 = 공부.
data class AppCategorySettings (val username: String) {
    val SETTING_LEISURE = 0
    val SETTING_PRODUCTIVE = 1

    private var categoryMap : MutableMap<String, Int> = mutableMapOf<String, Int>()

    fun addApp(appName: String) {
        categoryMap.put(appName, 0)
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

    fun getAppListFromUsageStats() {
        val appNameSet: MutableSet<String> = mutableSetOf<String>()
    }
}